import { useState } from 'react';
import { Bell, Moon, Sun, Monitor, Palette, Globe, Shield } from 'lucide-react';
import { useUIStore } from '@/store/uiStore';
import { useNotificationStore } from '@/store/notificationStore';
import { useAuthStore } from '@/store/authStore';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Switch } from '@/components/ui/switch';
import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Separator } from '@/components/ui/separator';
import { Badge } from '@/components/ui/badge';
import { toast } from 'sonner';

type Theme = 'light' | 'dark' | 'system';

export function ConfiguracionPage() {
  const { user } = useAuthStore();
  const { sidebarCollapsed, toggleSidebarCollapsed } = useUIStore();
  const { clearAll } = useNotificationStore();

  // Estados locales para configuraciones (en una app real, esto estaría en un store/backend)
  const [theme, setTheme] = useState<Theme>('system');
  const [notificationsEnabled, setNotificationsEnabled] = useState(true);
  const [soundEnabled, setSoundEnabled] = useState(true);
  const [language, setLanguage] = useState('es');

  const handleThemeChange = (newTheme: Theme) => {
    setTheme(newTheme);
    // Aplicar tema
    const root = document.documentElement;
    if (newTheme === 'dark') {
      root.classList.add('dark');
    } else if (newTheme === 'light') {
      root.classList.remove('dark');
    } else {
      // Sistema
      if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
        root.classList.add('dark');
      } else {
        root.classList.remove('dark');
      }
    }
    toast.success(`Tema cambiado a ${newTheme === 'system' ? 'automático' : newTheme}`);
  };

  const handleClearNotifications = () => {
    clearAll();
    toast.success('Notificaciones eliminadas');
  };

  const getThemeIcon = () => {
    switch (theme) {
      case 'light':
        return <Sun className="h-4 w-4" />;
      case 'dark':
        return <Moon className="h-4 w-4" />;
      default:
        return <Monitor className="h-4 w-4" />;
    }
  };

  return (
    <div className="space-y-6 max-w-3xl mx-auto">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Configuración</h2>
        <p className="text-muted-foreground">
          Personaliza la aplicación según tus preferencias
        </p>
      </div>

      {/* Apariencia */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Palette className="h-5 w-5" />
            Apariencia
          </CardTitle>
          <CardDescription>Personaliza el aspecto visual de la aplicación</CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <Label className="text-base">Tema</Label>
              <p className="text-sm text-muted-foreground">
                Selecciona el tema de la interfaz
              </p>
            </div>
            <Select value={theme} onValueChange={(v) => handleThemeChange(v as Theme)}>
              <SelectTrigger className="w-40">
                <SelectValue>
                  <span className="flex items-center gap-2">
                    {getThemeIcon()}
                    {theme === 'light' ? 'Claro' : theme === 'dark' ? 'Oscuro' : 'Sistema'}
                  </span>
                </SelectValue>
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="light">
                  <span className="flex items-center gap-2">
                    <Sun className="h-4 w-4" /> Claro
                  </span>
                </SelectItem>
                <SelectItem value="dark">
                  <span className="flex items-center gap-2">
                    <Moon className="h-4 w-4" /> Oscuro
                  </span>
                </SelectItem>
                <SelectItem value="system">
                  <span className="flex items-center gap-2">
                    <Monitor className="h-4 w-4" /> Sistema
                  </span>
                </SelectItem>
              </SelectContent>
            </Select>
          </div>

          <Separator />

          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <Label className="text-base">Barra lateral compacta</Label>
              <p className="text-sm text-muted-foreground">
                Mostrar solo iconos en la navegación
              </p>
            </div>
            <Switch checked={sidebarCollapsed} onCheckedChange={toggleSidebarCollapsed} />
          </div>
        </CardContent>
      </Card>

      {/* Notificaciones */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Bell className="h-5 w-5" />
            Notificaciones
          </CardTitle>
          <CardDescription>Configura cómo recibir alertas y avisos</CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <Label className="text-base">Notificaciones en la app</Label>
              <p className="text-sm text-muted-foreground">
                Recibir notificaciones dentro de la aplicación
              </p>
            </div>
            <Switch
              checked={notificationsEnabled}
              onCheckedChange={setNotificationsEnabled}
            />
          </div>

          <Separator />

          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <Label className="text-base">Sonidos</Label>
              <p className="text-sm text-muted-foreground">
                Reproducir sonido con las notificaciones
              </p>
            </div>
            <Switch
              checked={soundEnabled}
              onCheckedChange={setSoundEnabled}
              disabled={!notificationsEnabled}
            />
          </div>

          <Separator />

          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <Label className="text-base">Limpiar notificaciones</Label>
              <p className="text-sm text-muted-foreground">
                Eliminar todas las notificaciones almacenadas
              </p>
            </div>
            <Button variant="outline" size="sm" onClick={handleClearNotifications}>
              Limpiar todo
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Idioma y Región */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Globe className="h-5 w-5" />
            Idioma y Región
          </CardTitle>
          <CardDescription>Configura el idioma y formato de fechas</CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <Label className="text-base">Idioma</Label>
              <p className="text-sm text-muted-foreground">Idioma de la interfaz</p>
            </div>
            <Select value={language} onValueChange={setLanguage}>
              <SelectTrigger className="w-40">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="es">Español</SelectItem>
                <SelectItem value="en" disabled>
                  English (próximamente)
                </SelectItem>
              </SelectContent>
            </Select>
          </div>

          <Separator />

          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <Label className="text-base">Formato de fecha</Label>
              <p className="text-sm text-muted-foreground">DD/MM/YYYY</p>
            </div>
            <Badge variant="secondary">Colombia</Badge>
          </div>

          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <Label className="text-base">Zona horaria</Label>
              <p className="text-sm text-muted-foreground">
                {Intl.DateTimeFormat().resolvedOptions().timeZone}
              </p>
            </div>
            <Badge variant="outline">UTC-5</Badge>
          </div>
        </CardContent>
      </Card>

      {/* Información de la sesión */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Shield className="h-5 w-5" />
            Información de la Sesión
          </CardTitle>
          <CardDescription>Detalles de tu sesión actual</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-foreground">Usuario</span>
              <span className="font-medium">{user?.email}</span>
            </div>
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-foreground">Rol</span>
              <Badge>{user?.rol}</Badge>
            </div>
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-foreground">Empresa</span>
              <span className="font-medium">{user?.empresaNombre || 'N/A'}</span>
            </div>
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-foreground">Navegador</span>
              <span className="font-mono text-xs">{navigator.userAgent.split(' ').pop()}</span>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Versión de la app */}
      <div className="text-center text-sm text-muted-foreground">
        <p>Sistema de Trazabilidad de Frutas v1.0.0</p>
        <p>© 2026 - Trabajo de Fin de Máster</p>
      </div>
    </div>
  );
}

export default ConfiguracionPage;
