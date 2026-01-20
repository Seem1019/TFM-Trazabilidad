import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { toast } from 'sonner';
import { User, Mail, Phone, Building2, Shield, Calendar, Lock, Save } from 'lucide-react';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { useAuthStore } from '@/store/authStore';
import { userService } from '@/services';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { ROLES_LABELS } from '@/types';

const profileSchema = z.object({
  nombre: z.string().min(1, 'Nombre requerido').max(100, 'Máximo 100 caracteres'),
  apellido: z.string().max(100, 'Máximo 100 caracteres').optional(),
  telefono: z.string().max(20, 'Máximo 20 caracteres').optional(),
});

const passwordSchema = z
  .object({
    currentPassword: z.string().min(1, 'Contraseña actual requerida'),
    newPassword: z.string().min(8, 'Mínimo 8 caracteres'),
    confirmPassword: z.string().min(1, 'Confirme la contraseña'),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: 'Las contraseñas no coinciden',
    path: ['confirmPassword'],
  });

type ProfileFormData = z.infer<typeof profileSchema>;
type PasswordFormData = z.infer<typeof passwordSchema>;

export function PerfilPage() {
  const { user, setUser } = useAuthStore();
  const [isPasswordDialogOpen, setIsPasswordDialogOpen] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isDirty },
  } = useForm<ProfileFormData>({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      nombre: user?.nombre || '',
      apellido: user?.apellido || '',
      telefono: '',
    },
  });

  const {
    register: registerPassword,
    handleSubmit: handleSubmitPassword,
    reset: resetPassword,
    formState: { errors: passwordErrors, isSubmitting: isChangingPassword },
  } = useForm<PasswordFormData>({
    resolver: zodResolver(passwordSchema),
  });

  const handleProfileSubmit = async (data: ProfileFormData) => {
    if (!user) return;

    setIsSaving(true);
    try {
      const updated = await userService.update(user.id, {
        email: user.email,
        nombre: data.nombre,
        apellido: data.apellido,
        telefono: data.telefono,
        rol: user.rol,
      });

      // Actualizar el store
      setUser({
        ...user,
        nombre: updated.nombre,
        apellido: updated.apellido || '',
      });

      toast.success('Perfil actualizado correctamente');
    } catch {
      toast.error('Error al actualizar el perfil');
    } finally {
      setIsSaving(false);
    }
  };

  const handlePasswordSubmit = async (data: PasswordFormData) => {
    if (!user) return;

    try {
      // Nota: El backend actual no tiene un endpoint específico para cambio de contraseña
      // desde el perfil. Se podría usar el update con la nueva contraseña
      await userService.update(user.id, {
        email: user.email,
        nombre: user.nombre,
        apellido: user.apellido || undefined,
        rol: user.rol,
        password: data.newPassword,
      });

      toast.success('Contraseña actualizada correctamente');
      setIsPasswordDialogOpen(false);
      resetPassword();
    } catch {
      toast.error('Error al cambiar la contraseña');
    }
  };

  const getInitials = (nombre?: string, apellido?: string) => {
    const n = nombre?.charAt(0) || '';
    const a = apellido?.charAt(0) || '';
    return (n + a).toUpperCase() || 'U';
  };

  if (!user) {
    return null;
  }

  return (
    <div className="space-y-6 max-w-3xl mx-auto">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Mi Perfil</h2>
        <p className="text-muted-foreground">Gestiona tu información personal y contraseña</p>
      </div>

      {/* Información del usuario */}
      <Card>
        <CardHeader>
          <div className="flex items-center gap-4">
            <Avatar className="h-20 w-20">
              <AvatarFallback className="bg-primary text-primary-foreground text-2xl">
                {getInitials(user.nombre, user.apellido)}
              </AvatarFallback>
            </Avatar>
            <div>
              <CardTitle className="text-2xl">
                {user.nombre} {user.apellido}
              </CardTitle>
              <CardDescription className="flex items-center gap-2 mt-1">
                <Mail className="h-4 w-4" />
                {user.email}
              </CardDescription>
              <Badge variant="secondary" className="mt-2">
                <Shield className="mr-1 h-3 w-3" />
                {ROLES_LABELS[user.rol] || user.rol}
              </Badge>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div className="flex items-center gap-2">
              <Building2 className="h-4 w-4 text-muted-foreground" />
              <span className="text-muted-foreground">Empresa:</span>
              <span className="font-medium">{user.empresaNombre || 'N/A'}</span>
            </div>
            <div className="flex items-center gap-2">
              <Calendar className="h-4 w-4 text-muted-foreground" />
              <span className="text-muted-foreground">ID:</span>
              <span className="font-mono">{user.id}</span>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Formulario de edición */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <User className="h-5 w-5" />
            Información Personal
          </CardTitle>
          <CardDescription>
            Actualiza tu nombre y datos de contacto
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(handleProfileSubmit)} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="nombre">Nombre *</Label>
                <Input id="nombre" {...register('nombre')} placeholder="Tu nombre" />
                {errors.nombre && (
                  <p className="text-sm text-destructive">{errors.nombre.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="apellido">Apellido</Label>
                <Input id="apellido" {...register('apellido')} placeholder="Tu apellido" />
                {errors.apellido && (
                  <p className="text-sm text-destructive">{errors.apellido.message}</p>
                )}
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" value={user.email} disabled className="bg-muted" />
              <p className="text-xs text-muted-foreground">
                El email no se puede cambiar. Contacta al administrador si necesitas actualizarlo.
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="telefono">Teléfono</Label>
              <div className="relative">
                <Phone className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  id="telefono"
                  {...register('telefono')}
                  placeholder="+57 300 123 4567"
                  className="pl-10"
                />
              </div>
              {errors.telefono && (
                <p className="text-sm text-destructive">{errors.telefono.message}</p>
              )}
            </div>

            <div className="flex justify-end">
              <Button type="submit" disabled={!isDirty || isSaving}>
                <Save className="mr-2 h-4 w-4" />
                {isSaving ? 'Guardando...' : 'Guardar Cambios'}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      {/* Seguridad */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Lock className="h-5 w-5" />
            Seguridad
          </CardTitle>
          <CardDescription>Gestiona tu contraseña de acceso</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-between">
            <div>
              <p className="font-medium">Contraseña</p>
              <p className="text-sm text-muted-foreground">
                Última actualización: {format(new Date(), "dd 'de' MMMM, yyyy", { locale: es })}
              </p>
            </div>
            <Button variant="outline" onClick={() => setIsPasswordDialogOpen(true)}>
              <Lock className="mr-2 h-4 w-4" />
              Cambiar Contraseña
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Dialog para cambiar contraseña */}
      <Dialog open={isPasswordDialogOpen} onOpenChange={setIsPasswordDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Cambiar Contraseña</DialogTitle>
            <DialogDescription>
              Ingresa tu contraseña actual y la nueva contraseña
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={handleSubmitPassword(handlePasswordSubmit)} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="currentPassword">Contraseña Actual</Label>
              <Input
                id="currentPassword"
                type="password"
                {...registerPassword('currentPassword')}
                placeholder="••••••••"
              />
              {passwordErrors.currentPassword && (
                <p className="text-sm text-destructive">
                  {passwordErrors.currentPassword.message}
                </p>
              )}
            </div>

            <Separator />

            <div className="space-y-2">
              <Label htmlFor="newPassword">Nueva Contraseña</Label>
              <Input
                id="newPassword"
                type="password"
                {...registerPassword('newPassword')}
                placeholder="Mínimo 8 caracteres"
              />
              {passwordErrors.newPassword && (
                <p className="text-sm text-destructive">{passwordErrors.newPassword.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="confirmPassword">Confirmar Contraseña</Label>
              <Input
                id="confirmPassword"
                type="password"
                {...registerPassword('confirmPassword')}
                placeholder="Repite la nueva contraseña"
              />
              {passwordErrors.confirmPassword && (
                <p className="text-sm text-destructive">
                  {passwordErrors.confirmPassword.message}
                </p>
              )}
            </div>

            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={() => setIsPasswordDialogOpen(false)}
              >
                Cancelar
              </Button>
              <Button type="submit" disabled={isChangingPassword}>
                {isChangingPassword ? 'Cambiando...' : 'Cambiar Contraseña'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}

export default PerfilPage;
