import {
  MapPin,
  Layers,
  Package,
  Truck,
  TrendingUp,
  Clock,
} from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { useAuthStore } from '@/store/authStore';

interface StatCardProps {
  title: string;
  value: string | number;
  description: string;
  icon: React.ElementType;
  trend?: string;
}

function StatCard({ title, value, description, icon: Icon, trend }: StatCardProps) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium">{title}</CardTitle>
        <Icon className="h-4 w-4 text-muted-foreground" />
      </CardHeader>
      <CardContent>
        <div className="text-2xl font-bold">{value}</div>
        <p className="text-xs text-muted-foreground">
          {description}
          {trend && (
            <span className="ml-1 text-primary">
              <TrendingUp className="inline h-3 w-3" /> {trend}
            </span>
          )}
        </p>
      </CardContent>
    </Card>
  );
}

export function DashboardPage() {
  const { user } = useAuthStore();

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Dashboard</h2>
        <p className="text-muted-foreground">
          Bienvenido, {user?.nombre}. Aquí está el resumen de su operación.
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <StatCard
          title="Fincas Activas"
          value={12}
          description="Total de fincas registradas"
          icon={MapPin}
        />
        <StatCard
          title="Lotes en Producción"
          value={48}
          description="Lotes con cultivos activos"
          icon={Layers}
          trend="+4 este mes"
        />
        <StatCard
          title="Pallets Preparados"
          value={156}
          description="Listos para envío"
          icon={Package}
        />
        <StatCard
          title="Envíos en Tránsito"
          value={8}
          description="En camino al destino"
          icon={Truck}
        />
      </div>

      {/* Recent Activity */}
      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Clock className="h-5 w-5" />
              Actividad Reciente
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {[
                {
                  action: 'Nuevo lote registrado',
                  detail: 'Finca El Paraíso - Lote A-15',
                  time: 'Hace 2 horas',
                },
                {
                  action: 'Envío completado',
                  detail: 'ENV-2024-0089 - Rotterdam',
                  time: 'Hace 5 horas',
                },
                {
                  action: 'Recepción en planta',
                  detail: '2,500 kg - Mango Tommy',
                  time: 'Hace 8 horas',
                },
                {
                  action: 'Certificación renovada',
                  detail: 'GlobalG.A.P. - Finca Santa María',
                  time: 'Ayer',
                },
              ].map((item, index) => (
                <div
                  key={index}
                  className="flex items-start gap-4 rounded-lg border p-3"
                >
                  <div className="flex-1">
                    <p className="text-sm font-medium">{item.action}</p>
                    <p className="text-sm text-muted-foreground">{item.detail}</p>
                  </div>
                  <span className="text-xs text-muted-foreground whitespace-nowrap">
                    {item.time}
                  </span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Truck className="h-5 w-5" />
              Envíos Próximos
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {[
                {
                  code: 'ENV-2024-0092',
                  destination: 'Miami, USA',
                  date: '15 Ene 2026',
                  status: 'Preparando',
                },
                {
                  code: 'ENV-2024-0093',
                  destination: 'Rotterdam, NL',
                  date: '18 Ene 2026',
                  status: 'Documentación',
                },
                {
                  code: 'ENV-2024-0094',
                  destination: 'Londres, UK',
                  date: '20 Ene 2026',
                  status: 'Pendiente',
                },
              ].map((shipment, index) => (
                <div
                  key={index}
                  className="flex items-center justify-between rounded-lg border p-3"
                >
                  <div>
                    <p className="text-sm font-medium">{shipment.code}</p>
                    <p className="text-sm text-muted-foreground">
                      {shipment.destination}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm">{shipment.date}</p>
                    <span className="inline-flex items-center rounded-full bg-primary/10 px-2 py-0.5 text-xs font-medium text-primary">
                      {shipment.status}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

export default DashboardPage;
