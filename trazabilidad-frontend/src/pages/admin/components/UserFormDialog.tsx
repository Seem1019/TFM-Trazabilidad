import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import type { UserResponse, UserRequest } from '@/types';
import { ROLES_LIST } from '@/types';

const userSchema = z.object({
  email: z.string().email('Email inválido'),
  password: z.string().optional(),
  nombre: z.string().min(1, 'Nombre requerido').max(100, 'Máximo 100 caracteres'),
  apellido: z.string().max(100, 'Máximo 100 caracteres').optional(),
  telefono: z.string().max(20, 'Máximo 20 caracteres').optional(),
  rol: z.enum([
    'ADMIN',
    'PRODUCTOR',
    'OPERADOR_PLANTA',
    'OPERADOR_LOGISTICA',
    'AUDITOR',
  ]),
  activo: z.boolean(),
});

type UserFormData = z.infer<typeof userSchema>;

interface UserFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  user: UserResponse | null;
  onSubmit: (data: UserRequest) => Promise<void>;
}

export function UserFormDialog({
  open,
  onOpenChange,
  user,
  onSubmit,
}: UserFormDialogProps) {
  const isEditing = !!user;

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<UserFormData>({
    resolver: zodResolver(userSchema),
    defaultValues: {
      email: '',
      password: '',
      nombre: '',
      apellido: '',
      telefono: '',
      rol: 'OPERADOR_PLANTA',
      activo: true,
    },
  });

  const activo = watch('activo');
  const rol = watch('rol');

  useEffect(() => {
    if (user) {
      reset({
        email: user.email,
        password: '',
        nombre: user.nombre,
        apellido: user.apellido || '',
        telefono: user.telefono || '',
        rol: user.rol,
        activo: user.activo,
      });
    } else {
      reset({
        email: '',
        password: '',
        nombre: '',
        apellido: '',
        telefono: '',
        rol: 'OPERADOR_PLANTA',
        activo: true,
      });
    }
  }, [user, reset]);

  const handleFormSubmit = async (data: UserFormData) => {
    const request: UserRequest = {
      email: data.email,
      nombre: data.nombre,
      apellido: data.apellido || undefined,
      telefono: data.telefono || undefined,
      rol: data.rol,
      activo: data.activo,
    };

    // Solo incluir password si se proporcionó
    if (data.password && data.password.trim() !== '') {
      request.password = data.password;
    }

    await onSubmit(request);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>
            {isEditing ? 'Editar Usuario' : 'Nuevo Usuario'}
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="nombre">Nombre *</Label>
              <Input
                id="nombre"
                {...register('nombre')}
                placeholder="Juan"
              />
              {errors.nombre && (
                <p className="text-sm text-destructive">{errors.nombre.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="apellido">Apellido</Label>
              <Input
                id="apellido"
                {...register('apellido')}
                placeholder="Pérez"
              />
              {errors.apellido && (
                <p className="text-sm text-destructive">{errors.apellido.message}</p>
              )}
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="email">Email *</Label>
            <Input
              id="email"
              type="email"
              {...register('email')}
              placeholder="usuario@empresa.com"
            />
            {errors.email && (
              <p className="text-sm text-destructive">{errors.email.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="password">
              Contraseña {isEditing ? '(dejar vacío para mantener)' : '*'}
            </Label>
            <Input
              id="password"
              type="password"
              {...register('password')}
              placeholder={isEditing ? '••••••••' : 'Mínimo 8 caracteres'}
            />
            {errors.password && (
              <p className="text-sm text-destructive">{errors.password.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="telefono">Teléfono</Label>
            <Input
              id="telefono"
              {...register('telefono')}
              placeholder="+57 300 123 4567"
            />
            {errors.telefono && (
              <p className="text-sm text-destructive">{errors.telefono.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="rol">Rol *</Label>
            <Select value={rol} onValueChange={(value) => setValue('rol', value as UserFormData['rol'])}>
              <SelectTrigger>
                <SelectValue placeholder="Seleccione un rol" />
              </SelectTrigger>
              <SelectContent>
                {ROLES_LIST.map((role) => (
                  <SelectItem key={role.value} value={role.value}>
                    {role.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.rol && (
              <p className="text-sm text-destructive">{errors.rol.message}</p>
            )}
          </div>

          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <Label htmlFor="activo">Estado</Label>
              <p className="text-sm text-muted-foreground">
                {activo ? 'Usuario activo' : 'Usuario inactivo'}
              </p>
            </div>
            <Switch
              id="activo"
              checked={activo}
              onCheckedChange={(checked) => setValue('activo', checked)}
            />
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
            >
              Cancelar
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Guardando...' : isEditing ? 'Actualizar' : 'Crear'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

export default UserFormDialog;
