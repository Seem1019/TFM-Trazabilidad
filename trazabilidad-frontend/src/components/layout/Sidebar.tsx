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
  ClipboardCheck,
  Truck,
  CalendarClock,
  FileText,
  Search,
  Users,
  ChevronLeft,
  ChevronRight,
  Leaf,
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { useUIStore } from '@/store/uiStore';
import { usePermissions, type Module } from '@/hooks/usePermissions';
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
  module: Module;
}

interface NavGroup {
  title: string;
  items: NavItem[];
}

const navGroups: NavGroup[] = [
  {
    title: 'Principal',
    items: [
      { title: 'Dashboard', href: '/', icon: LayoutDashboard, module: 'dashboard' },
    ],
  },
  {
    title: 'Producción',
    items: [
      { title: 'Fincas', href: '/fincas', icon: MapPin, module: 'fincas' },
      { title: 'Lotes', href: '/lotes', icon: Layers, module: 'lotes' },
      { title: 'Cosechas', href: '/cosechas', icon: Sprout, module: 'cosechas' },
      { title: 'Certificaciones', href: '/certificaciones', icon: Award, module: 'certificaciones' },
      { title: 'Actividades', href: '/actividades', icon: Leaf, module: 'actividades' },
    ],
  },
  {
    title: 'Empaque',
    items: [
      { title: 'Recepciones', href: '/recepciones', icon: Warehouse, module: 'recepciones' },
      { title: 'Clasificación', href: '/clasificacion', icon: Package, module: 'clasificacion' },
      { title: 'Etiquetas', href: '/etiquetas', icon: Tags, module: 'etiquetas' },
      { title: 'Pallets', href: '/pallets', icon: Boxes, module: 'pallets' },
      { title: 'Control Calidad', href: '/control-calidad', icon: ClipboardCheck, module: 'control-calidad' },
    ],
  },
  {
    title: 'Logística',
    items: [
      { title: 'Envíos', href: '/envios', icon: Truck, module: 'envios' },
      { title: 'Eventos', href: '/eventos', icon: CalendarClock, module: 'eventos' },
      { title: 'Documentos', href: '/documentos', icon: FileText, module: 'documentos' },
    ],
  },
  {
    title: 'Trazabilidad',
    items: [
      { title: 'Consulta', href: '/trazabilidad', icon: Search, module: 'trazabilidad' },
    ],
  },
  {
    title: 'Administración',
    items: [
      { title: 'Usuarios', href: '/usuarios', icon: Users, module: 'usuarios' },
    ],
  },
];

export function Sidebar() {
  const location = useLocation();
  const { sidebarCollapsed, toggleSidebarCollapsed } = useUIStore();
  const { canAccess } = usePermissions();

  const isActiveRoute = (href: string) => {
    if (href === '/') {
      return location.pathname === '/';
    }
    return location.pathname.startsWith(href);
  };

  const canAccessItem = (item: NavItem) => {
    return canAccess(item.module);
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
