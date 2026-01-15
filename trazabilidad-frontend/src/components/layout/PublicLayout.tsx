import { Outlet } from 'react-router-dom';
import { Leaf } from 'lucide-react';

export function PublicLayout() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-primary-100">
      <div className="flex min-h-screen flex-col items-center justify-center p-4">
        <div className="mb-8 flex items-center gap-3">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary text-primary-foreground">
            <Leaf className="h-7 w-7" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Trazabilidad</h1>
            <p className="text-sm text-gray-600">Exportación de Frutas</p>
          </div>
        </div>
        <Outlet />
      </div>
    </div>
  );
}

export default PublicLayout;
