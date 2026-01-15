import { Link, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  MapPin,
  Layers,
  Sprout,
  Award,
  Warehouse,
  Package,
  Tags,
  Boxes,
  Truck,
  FileText,
  Search,
  Users,
  ChevronLeft,
  ChevronRight,
  Leaf,
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { useUIStore } from '@/store/uiStore';
import { useAuthStore } from '@/store/authStore';
import { Button } from '@/components/ui/button';
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
  TooltipProvider,
} from '@/components/ui/tooltip';
import { Separator } from '@/components/ui/separator';

interface NavItem {
  title: string;
  href: string;
  icon: React.ElementType;
  roles?: string[];
}

interface NavGroup {
  title: string;
  items: NavItem[];
}

const navGroups: NavGroup[] = [
  {
    title: 'Principal',
    items: [
      { title: 'Dashboard', href: '/', icon: LayoutDashboard },
    ],
  },
  {
    title: 'Producción',
    items: [
      { title: 'Fincas', href: '/fincas', icon: MapPin },
      { title: 'Lotes', href: '/lotes', icon: Layers },
      { title: 'Cosechas', href: '/cosechas', icon: Sprout },
      { title: 'Certificaciones', href: '/certificaciones', icon: Award },
      { title: 'Actividades', href: '/actividades', icon: Leaf },
    ],
  },
  {
    title: 'Empaque',
    items: [
      { title: 'Recepciones', href: '/recepciones', icon: Warehouse },
      { title: 'Clasificación', href: '/clasificacion', icon: Package },
      { title: 'Etiquetas', href: '/etiquetas', icon: Tags },
      { title: 'Pallets', href: '/pallets', icon: Boxes },
    ],
  },
  {
    title: 'Logística',
    items: [
      { title: 'Envíos', href: '/envios', icon: Truck },
      { title: 'Documentos', href: '/documentos', icon: FileText },
    ],
  },
  {
    title: 'Trazabilidad',
    items: [
      { title: 'Consulta', href: '/trazabilidad', icon: Search },
    ],
  },
  {
    title: 'Administración',
    items: [
      { title: 'Usuarios', href: '/usuarios', icon: Users, roles: ['ADMIN_SISTEMA', 'ADMIN_EMPRESA'] },
    ],
  },
];

export function Sidebar() {
  const location = useLocation();
  const { sidebarCollapsed, toggleSidebarCollapsed } = useUIStore();
  const { user } = useAuthStore();

  const isActiveRoute = (href: string) => {
    if (href === '/') {
      return location.pathname === '/';
    }
    return location.pathname.startsWith(href);
  };

  const canAccessItem = (item: NavItem) => {
    if (!item.roles) return true;
    if (!user) return false;
    return item.roles.includes(user.rol);
  };

  return (
    <TooltipProvider delayDuration={0}>
      <aside
        className={cn(
          'fixed left-0 top-0 z-40 h-screen border-r bg-sidebar transition-all duration-300',
          sidebarCollapsed ? 'w-16' : 'w-64'
        )}
      >
        <div className="flex h-full flex-col">
          {/* Logo */}
          <div className="flex h-16 items-center justify-between border-b px-4">
            <Link to="/" className="flex items-center gap-2">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
                <Leaf className="h-5 w-5" />
              </div>
              {!sidebarCollapsed && (
                <span className="font-semibold text-sidebar-foreground">
                  Trazabilidad
                </span>
              )}
            </Link>
          </div>

          {/* Navigation */}
          <nav className="flex-1 overflow-y-auto px-3 py-4">
            {navGroups.map((group, groupIndex) => (
              <div key={group.title} className="mb-4">
                {!sidebarCollapsed && (
                  <h3 className="mb-2 px-2 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                    {group.title}
                  </h3>
                )}
                {groupIndex > 0 && sidebarCollapsed && (
                  <Separator className="my-2" />
                )}
                <ul className="space-y-1">
                  {group.items.filter(canAccessItem).map((item) => {
                    const Icon = item.icon;
                    const isActive = isActiveRoute(item.href);

                    const linkContent = (
                      <Link
                        to={item.href}
                        className={cn(
                          'flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors',
                          isActive
                            ? 'bg-sidebar-accent text-sidebar-accent-foreground font-medium'
                            : 'text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground',
                          sidebarCollapsed && 'justify-center px-2'
                        )}
                      >
                        <Icon className="h-4 w-4 shrink-0" />
                        {!sidebarCollapsed && <span>{item.title}</span>}
                      </Link>
                    );

                    return (
                      <li key={item.href}>
                        {sidebarCollapsed ? (
                          <Tooltip>
                            <TooltipTrigger asChild>
                              {linkContent}
                            </TooltipTrigger>
                            <TooltipContent side="right">
                              {item.title}
                            </TooltipContent>
                          </Tooltip>
                        ) : (
                          linkContent
                        )}
                      </li>
                    );
                  })}
                </ul>
              </div>
            ))}
          </nav>

          {/* Collapse button */}
          <div className="border-t p-3">
            <Button
              variant="ghost"
              size="sm"
              className="w-full justify-center"
              onClick={toggleSidebarCollapsed}
            >
              {sidebarCollapsed ? (
                <ChevronRight className="h-4 w-4" />
              ) : (
                <>
                  <ChevronLeft className="h-4 w-4" />
                  <span className="ml-2">Colapsar</span>
                </>
              )}
            </Button>
          </div>
        </div>
      </aside>
    </TooltipProvider>
  );
}

export default Sidebar;
