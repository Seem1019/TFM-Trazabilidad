import { useCallback, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  MapPin,
  Layers,
  Package,
  Truck,
  TrendingUp,
  Clock,
  RefreshCw,
  ExternalLink,
} from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { useAuthStore } from '@/store/authStore';
import { useNotificationStore } from '@/store/notificationStore';
import { dashboardService } from '@/services';
import { useFetch } from '@/hooks/useFetch';
import type {
  DashboardStats,
  RecentActivity,
  UpcomingShipment,
} from '@/services/dashboardService';

interface StatCardProps {
  title: string;
  value: string | number;
  description: string;
  icon: React.ElementType;
  trend?: string;
  loading?: boolean;
  link?: string;
}

function StatCard({ title, value, description, icon: Icon, trend, loading, link }: StatCardProps) {
  const content = (
    <Card className={link ? 'hover:bg-muted/50 transition-colors cursor-pointer' : ''}>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium">{title}</CardTitle>
        <Icon className="h-4 w-4 text-muted-foreground" />
      </CardHeader>
      <CardContent>
        {loading ? (
          <>
            <Skeleton className="h-8 w-16 mb-1" />
            <Skeleton className="h-4 w-32" />
          </>
        ) : (
          <>
            <div className="text-2xl font-bold">{value}</div>
            <p className="text-xs text-muted-foreground">
              {description}
              {trend && (
                <span className="ml-1 text-primary">
                  <TrendingUp className="inline h-3 w-3" /> {trend}
                </span>
              )}
            </p>
          </>
        )}
      </CardContent>
    </Card>
  );

  if (link) {
    return <Link to={link}>{content}</Link>;
  }
  return content;
}

export function DashboardPage() {
  const { user } = useAuthStore();
  const { addNotification } = useNotificationStore();

  const {
    data: stats,
    isLoading: loadingStats,
    refetch: refetchStats,
  } = useFetch<DashboardStats>(
    useCallback(() => dashboardService.getStats(), []),
    []
  );

  const {
    data: recentActivity,
    isLoading: loadingActivity,
    refetch: refetchActivity,
  } = useFetch<RecentActivity[]>(
    useCallback(() => dashboardService.getRecentActivity(), []),
    []
  );

  const {
    data: upcomingShipments,
    isLoading: loadingShipments,
    refetch: refetchShipments,
  } = useFetch<UpcomingShipment[]>(
    useCallback(() => dashboardService.getUpcomingShipments(), []),
    []
  );

  // Notificación de bienvenida al cargar el dashboard (solo una vez)
  useEffect(() => {
    const hasShownWelcome = sessionStorage.getItem('dashboard-welcome');
    if (!hasShownWelcome && user) {
      addNotification(
        'info',
        'Bienvenido',
        `Has iniciado sesión como ${user.nombre}`,
        '/'
      );
      sessionStorage.setItem('dashboard-welcome', 'true');
    }
  }, [user, addNotification]);

  const handleRefresh = () => {
    refetchStats();
    refetchActivity();
    refetchShipments();
  };

  const getStatusVariant = (status: string) => {
    switch (status.toLowerCase()) {
      case 'preparando':
        return 'secondary';
      case 'listo':
        return 'success';
      case 'en tránsito':
        return 'default';
      case 'pendiente':
        return 'outline';
      default:
        return 'secondary';
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Dashboard</h2>
          <p className="text-muted-foreground">
            Bienvenido, {user?.nombre}. Aquí está el resumen de su operación.
          </p>
        </div>
        <Button variant="outline" size="sm" onClick={handleRefresh}>
          <RefreshCw className="mr-2 h-4 w-4" />
          Actualizar
        </Button>
      </div>

      {/* Stats Grid */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <StatCard
          title="Fincas Activas"
          value={stats?.fincasActivas ?? 0}
          description="Total de fincas registradas"
          icon={MapPin}
          loading={loadingStats}
          link="/fincas"
        />
        <StatCard
          title="Lotes en Producción"
          value={stats?.lotesEnProduccion ?? 0}
          description="Lotes con cultivos activos"
          icon={Layers}
          loading={loadingStats}
          link="/lotes"
        />
        <StatCard
          title="Pallets Preparados"
          value={stats?.palletsPreparados ?? 0}
          description="Listos para envío"
          icon={Package}
          loading={loadingStats}
          link="/pallets"
        />
        <StatCard
          title="Envíos en Tránsito"
          value={stats?.enviosEnTransito ?? 0}
          description="En camino al destino"
          icon={Truck}
          loading={loadingStats}
          link="/envios"
        />
      </div>

      {/* Recent Activity & Upcoming Shipments */}
      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Clock className="h-5 w-5" />
              Actividad Reciente
            </CardTitle>
          </CardHeader>
          <CardContent>
            {loadingActivity ? (
              <div className="space-y-4">
                {[1, 2, 3].map((i) => (
                  <div key={i} className="flex items-start gap-4 rounded-lg border p-3">
                    <div className="flex-1">
                      <Skeleton className="h-4 w-32 mb-2" />
                      <Skeleton className="h-3 w-48" />
                    </div>
                    <Skeleton className="h-3 w-16" />
                  </div>
                ))}
              </div>
            ) : recentActivity && recentActivity.length > 0 ? (
              <div className="space-y-4">
                {recentActivity.map((item) => (
                  <Link
                    key={item.id}
                    to={item.link || '#'}
                    className="flex items-start gap-4 rounded-lg border p-3 hover:bg-muted/50 transition-colors"
                  >
                    <div className="flex-1">
                      <p className="text-sm font-medium">{item.action}</p>
                      <p className="text-sm text-muted-foreground">{item.detail}</p>
                    </div>
                    <span className="text-xs text-muted-foreground whitespace-nowrap">
                      {item.time}
                    </span>
                  </Link>
                ))}
              </div>
            ) : (
              <div className="flex flex-col items-center justify-center py-8 text-center">
                <Clock className="h-8 w-8 text-muted-foreground mb-2" />
                <p className="text-sm text-muted-foreground">No hay actividad reciente</p>
                <p className="text-xs text-muted-foreground">
                  La actividad aparecerá aquí cuando registres cosechas, recepciones o
                  certificaciones
                </p>
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center justify-between">
              <span className="flex items-center gap-2">
                <Truck className="h-5 w-5" />
                Envíos Próximos
              </span>
              <Button variant="ghost" size="sm" asChild>
                <Link to="/envios">
                  <ExternalLink className="h-4 w-4" />
                </Link>
              </Button>
            </CardTitle>
          </CardHeader>
          <CardContent>
            {loadingShipments ? (
              <div className="space-y-4">
                {[1, 2, 3].map((i) => (
                  <div
                    key={i}
                    className="flex items-center justify-between rounded-lg border p-3"
                  >
                    <div>
                      <Skeleton className="h-4 w-24 mb-2" />
                      <Skeleton className="h-3 w-32" />
                    </div>
                    <div className="text-right">
                      <Skeleton className="h-4 w-20 mb-2" />
                      <Skeleton className="h-5 w-16" />
                    </div>
                  </div>
                ))}
              </div>
            ) : upcomingShipments && upcomingShipments.length > 0 ? (
              <div className="space-y-4">
                {upcomingShipments.map((shipment) => (
                  <Link
                    key={shipment.id}
                    to={`/envios?id=${shipment.id}`}
                    className="flex items-center justify-between rounded-lg border p-3 hover:bg-muted/50 transition-colors"
                  >
                    <div>
                      <p className="text-sm font-medium">{shipment.code}</p>
                      <p className="text-sm text-muted-foreground">{shipment.destination}</p>
                    </div>
                    <div className="text-right">
                      <p className="text-sm">{shipment.date}</p>
                      <Badge variant={getStatusVariant(shipment.status)}>{shipment.status}</Badge>
                    </div>
                  </Link>
                ))}
              </div>
            ) : (
              <div className="flex flex-col items-center justify-center py-8 text-center">
                <Truck className="h-8 w-8 text-muted-foreground mb-2" />
                <p className="text-sm text-muted-foreground">No hay envíos programados</p>
                <Button variant="link" size="sm" asChild className="mt-2">
                  <Link to="/envios">Crear nuevo envío</Link>
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

export default DashboardPage;
