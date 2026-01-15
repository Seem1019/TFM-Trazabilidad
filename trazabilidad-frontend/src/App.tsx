import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'sonner';
import { MainLayout, PublicLayout } from '@/components/layout';
import { ProtectedRoute } from '@/components/shared';

// Auth Pages
import LoginPage from '@/pages/auth/LoginPage';
import ForgotPasswordPage from '@/pages/auth/ForgotPasswordPage';

// Dashboard
import DashboardPage from '@/pages/dashboard/DashboardPage';

// Producción
import FincasPage from '@/pages/produccion/FincasPage';
import LotesPage from '@/pages/produccion/LotesPage';
import CosechasPage from '@/pages/produccion/CosechasPage';
import CertificacionesPage from '@/pages/produccion/CertificacionesPage';
import ActividadesPage from '@/pages/produccion/ActividadesPage';

// Empaque
import {
  RecepcionesPage,
  ClasificacionPage,
  EtiquetasPage,
  PalletsPage,
  ControlCalidadPage,
} from '@/pages/empaque';

// Placeholder para páginas pendientes
function PlaceholderPage({ title }: { title: string }) {
  return (
    <div className="flex flex-col items-center justify-center min-h-[400px]">
      <h1 className="text-2xl font-bold mb-2">{title}</h1>
      <p className="text-muted-foreground">Página en construcción</p>
    </div>
  );
}

function App() {
  return (
    <>
      <Toaster position="top-right" richColors />
      <BrowserRouter>
        <Routes>
          {/* Rutas públicas (sin autenticación) */}
          <Route element={<PublicLayout />}>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          </Route>

          {/* Ruta pública de trazabilidad (sin layout especial) */}
          <Route
            path="/public/trazabilidad/:uuid"
            element={<PlaceholderPage title="Trazabilidad Pública" />}
          />

          {/* Rutas protegidas (requieren autenticación) */}
          <Route
            element={
              <ProtectedRoute>
                <MainLayout />
              </ProtectedRoute>
            }
          >
            <Route path="/" element={<DashboardPage />} />

            {/* Producción */}
            <Route path="/fincas" element={<FincasPage />} />
            <Route path="/lotes" element={<LotesPage />} />
            <Route path="/cosechas" element={<CosechasPage />} />
            <Route path="/certificaciones" element={<CertificacionesPage />} />
            <Route path="/actividades" element={<ActividadesPage />} />

            {/* Empaque */}
            <Route path="/recepciones" element={<RecepcionesPage />} />
            <Route path="/clasificacion" element={<ClasificacionPage />} />
            <Route path="/etiquetas" element={<EtiquetasPage />} />
            <Route path="/pallets" element={<PalletsPage />} />
            <Route path="/control-calidad" element={<ControlCalidadPage />} />

            {/* Logística */}
            <Route path="/envios" element={<PlaceholderPage title="Envíos" />} />
            <Route
              path="/eventos"
              element={<PlaceholderPage title="Eventos Logísticos" />}
            />
            <Route
              path="/documentos"
              element={<PlaceholderPage title="Documentos de Exportación" />}
            />

            {/* Trazabilidad */}
            <Route
              path="/trazabilidad"
              element={<PlaceholderPage title="Consulta de Trazabilidad" />}
            />

            {/* Admin */}
            <Route
              path="/usuarios"
              element={
                <ProtectedRoute allowedRoles={['ADMIN_SISTEMA', 'ADMIN_EMPRESA']}>
                  <PlaceholderPage title="Gestión de Usuarios" />
                </ProtectedRoute>
              }
            />
          </Route>

          {/* Ruta por defecto - redirigir al dashboard */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </>
  );
}

export default App;
