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

// Logística
import {
  EnviosPage,
  EventosPage,
  DocumentosPage,
} from '@/pages/logistica';

// Trazabilidad
import {
  TrazabilidadPublicaPage,
  TrazabilidadInternaPage,
} from '@/pages/trazabilidad';

// Admin
import { UsuariosPage } from '@/pages/admin';

// Profile & Settings
import { PerfilPage } from '@/pages/profile';
import { ConfiguracionPage } from '@/pages/settings';

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
            element={<TrazabilidadPublicaPage />}
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
            <Route path="/envios" element={<EnviosPage />} />
            <Route path="/eventos" element={<EventosPage />} />
            <Route path="/documentos" element={<DocumentosPage />} />

            {/* Trazabilidad */}
            <Route path="/trazabilidad" element={<TrazabilidadInternaPage />} />

            {/* Admin */}
            <Route
              path="/usuarios"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <UsuariosPage />
                </ProtectedRoute>
              }
            />

            {/* Perfil y Configuración */}
            <Route path="/perfil" element={<PerfilPage />} />
            <Route path="/configuracion" element={<ConfiguracionPage />} />
          </Route>

          {/* Ruta por defecto - redirigir al dashboard */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </>
  );
}

export default App;
