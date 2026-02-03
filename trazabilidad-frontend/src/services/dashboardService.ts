import { fincaService } from './fincaService';
import { loteService } from './loteService';
import { palletService } from './palletService';
import { envioService } from './envioService';
import { cosechaService } from './cosechaService';
import { recepcionService } from './recepcionService';
import type { Envio, Cosecha, Finca, Lote, Pallet, RecepcionPlanta } from '@/types';

export interface DashboardStats {
  fincasActivas: number;
  lotesEnProduccion: number;
  palletsPreparados: number;
  enviosEnTransito: number;
}

export interface RecentActivity {
  id: string;
  action: string;
  detail: string;
  time: string;
  link?: string;
}

export interface UpcomingShipment {
  id: number;
  code: string;
  destination: string;
  date: string;
  status: string;
}

export const dashboardService = {
  getStats: async (): Promise<DashboardStats> => {
    try {
      const [fincas, lotes, pallets, envios] = await Promise.all([
        fincaService.getAll().catch(() => [] as Finca[]),
        loteService.getAll().catch(() => [] as Lote[]),
        palletService.getAll().catch(() => [] as Pallet[]),
        envioService.getAll().catch(() => [] as Envio[]),
      ]);

      const fincasActivas = fincas.filter((f) => f.activo).length;
      // Incluir lotes activos y en producción
      const lotesEnProduccion = lotes.filter((l) => l.activo && (l.estadoLote === 'ACTIVO' || l.estadoLote === 'EN_PRODUCCION')).length;
      // Incluir pallets armados, preparados y listos para envío
      const palletsPreparados = pallets.filter(
        (p) => p.estadoPallet === 'ARMADO' || p.estadoPallet === 'PREPARADO' || p.estadoPallet === 'LISTO_ENVIO'
      ).length;
      const enviosEnTransito = envios.filter((e) => e.estado === 'EN_TRANSITO').length;

      return {
        fincasActivas,
        lotesEnProduccion,
        palletsPreparados,
        enviosEnTransito,
      };
    } catch {
      return {
        fincasActivas: 0,
        lotesEnProduccion: 0,
        palletsPreparados: 0,
        enviosEnTransito: 0,
      };
    }
  },

  getRecentActivity: async (): Promise<RecentActivity[]> => {
    try {
      const [cosechas, recepciones] = await Promise.all([
        cosechaService.getAll().catch(() => [] as Cosecha[]),
        recepcionService.getAll().catch(() => [] as RecepcionPlanta[]),
      ]);

      const activities: RecentActivity[] = [];

      // Agregar cosechas recientes
      cosechas.slice(0, 3).forEach((c: Cosecha) => {
        activities.push({
          id: `cosecha-${c.id}`,
          action: 'Nueva cosecha registrada',
          detail: `${c.loteNombre || 'Lote'} - ${c.cantidadCosechada?.toLocaleString() || 0} ${c.unidadMedida || 'kg'}`,
          time: formatRelativeTime(c.fechaCosecha),
          link: '/cosechas',
        });
      });

      // Agregar recepciones recientes
      recepciones.slice(0, 3).forEach((r: RecepcionPlanta) => {
        activities.push({
          id: `recepcion-${r.id}`,
          action: 'Recepción en planta',
          detail: `${r.cantidadRecibida?.toLocaleString() || 0} kg - ${r.fincaNombre || ''}`,
          time: formatRelativeTime(r.fechaRecepcion),
          link: '/recepciones',
        });
      });

      // Ordenar por fecha y limitar
      return activities.slice(0, 5);
    } catch {
      return [];
    }
  },

  getUpcomingShipments: async (): Promise<UpcomingShipment[]> => {
    try {
      const envios = await envioService.getAll();

      return envios
        .filter((e) => !e.hashCierre && e.estado !== 'ENTREGADO' && e.estado !== 'CANCELADO')
        .slice(0, 4)
        .map((e: Envio) => ({
          id: e.id,
          code: e.codigoEnvio,
          destination: `${e.puertoDestino || ''}, ${e.paisDestino || ''}`.trim().replace(/^,\s*/, ''),
          date: formatDate(e.fechaSalidaEstimada),
          status: getEstadoLabel(e.estado),
        }));
    } catch {
      return [];
    }
  },
};

function formatRelativeTime(dateStr?: string): string {
  if (!dateStr) return 'Reciente';
  try {
    const date = new Date(dateStr);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffDays = Math.floor(diffHours / 24);

    if (diffHours < 1) return 'Hace unos minutos';
    if (diffHours < 24) return `Hace ${diffHours} hora${diffHours > 1 ? 's' : ''}`;
    if (diffDays < 7) return `Hace ${diffDays} día${diffDays > 1 ? 's' : ''}`;
    return date.toLocaleDateString('es-ES', { day: 'numeric', month: 'short' });
  } catch {
    return 'Reciente';
  }
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return '-';
  try {
    return new Date(dateStr).toLocaleDateString('es-ES', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
    });
  } catch {
    return dateStr;
  }
}

function getEstadoLabel(estado?: string): string {
  const labels: Record<string, string> = {
    CREADO: 'Pendiente',
    EN_PREPARACION: 'Preparando',
    LISTO_ENVIO: 'Listo',
    EN_TRANSITO: 'En tránsito',
    EN_PUERTO_ORIGEN: 'Puerto origen',
    EN_PUERTO_DESTINO: 'Puerto destino',
    ENTREGADO: 'Entregado',
    CANCELADO: 'Cancelado',
  };
  return estado ? labels[estado] || estado : '-';
}

export default dashboardService;
