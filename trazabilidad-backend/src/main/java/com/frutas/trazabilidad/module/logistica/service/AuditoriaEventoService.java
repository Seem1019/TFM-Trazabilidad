package com.frutas.trazabilidad.module.logistica.service;

import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.repository.UserRepository;
import com.frutas.trazabilidad.module.logistica.dto.AuditoriaEventoResponse;
import com.frutas.trazabilidad.module.logistica.entity.AuditoriaEvento;
import com.frutas.trazabilidad.module.logistica.entity.Envio;
import com.frutas.trazabilidad.module.logistica.mapper.AuditoriaEventoMapper;
import com.frutas.trazabilidad.module.logistica.repository.AuditoriaEventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditoriaEventoService {

    private final AuditoriaEventoRepository auditoriaRepository;
    private final UserRepository userRepository;
    private final AuditoriaEventoMapper auditoriaMapper;

    /**
     * Registrar creación de entidad.
     */
    @Transactional
    public void registrarCreacion(String tipoEntidad, Long entidadId, String codigoEntidad,
                                  String descripcion, User usuario) {
        registrarEvento(
                usuario,
                tipoEntidad,
                entidadId,
                codigoEntidad,
                "CREATE",
                descripcion,
                null,
                null,
                "INFO",
                false
        );
    }

    /**
     * Registrar actualización de entidad.
     */
    @Transactional
    public void registrarActualizacion(String tipoEntidad, Long entidadId, String codigoEntidad,
                                       String descripcion, String datosAnteriores, String datosNuevos,
                                       User usuario) {
        registrarEvento(
                usuario,
                tipoEntidad,
                entidadId,
                codigoEntidad,
                "UPDATE",
                descripcion,
                datosAnteriores,
                datosNuevos,
                "INFO",
                false
        );
    }

    /**
     * Registrar eliminación de entidad.
     */
    @Transactional
    public void registrarEliminacion(String tipoEntidad, Long entidadId, String codigoEntidad,
                                     String descripcion, User usuario) {
        registrarEvento(
                usuario,
                tipoEntidad,
                entidadId,
                codigoEntidad,
                "DELETE",
                descripcion,
                null,
                null,
                "WARNING",
                false
        );
    }

    /**
     * Registrar cierre de envío (CRÍTICO - va a blockchain).
     */
    @Transactional
    public void registrarCierreEnvio(Envio envio, User usuario) {
        String descripcion = String.format(
                "Cierre de envío %s con %d pallets, peso total: %.2f kg",
                envio.getCodigoEnvio(),
                envio.getNumeroPallets(),
                envio.getPesoNetoTotal()
        );

        String datosNuevos = String.format(
                "{hashCierre:'%s',fechaCierre:'%s',estado: '%s'}",
                envio.getHashCierre(),
                envio.getFechaCierre(),
                envio.getEstado()
        );

        registrarEvento(
                usuario,
                "ENVIO",
                envio.getId(),
                envio.getCodigoEnvio(),
                "CLOSE",
                descripcion,
                null,
                datosNuevos,
                "CRITICAL",
                true  // Va a la cadena blockchain
        );
    }

    /**
     * Listar eventos de auditoría por empresa.
     */
    @Transactional(readOnly = true)
    public List<AuditoriaEventoResponse> listarPorEmpresa() {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return auditoriaRepository.findByEmpresaIdOrderByFechaDesc(usuario.getEmpresa().getId())
                .stream()
                .map(this::toResponseConValidacion)
                .collect(Collectors.toList());
    }

    /**
     * Listar eventos de una entidad específica.
     */
    @Transactional(readOnly = true)
    public List<AuditoriaEventoResponse> listarPorEntidad(String tipoEntidad, Long entidadId) {
        return auditoriaRepository.findByTipoEntidadAndEntidadId(tipoEntidad, entidadId)
                .stream()
                .map(this::toResponseConValidacion)
                .collect(Collectors.toList());
    }

    /**
     * Listar cadena blockchain de la empresa.
     */
    @Transactional(readOnly = true)
    public List<AuditoriaEventoResponse> listarCadenaBlockchain() {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return auditoriaRepository.findCadenaBlockchainByEmpresaId(usuario.getEmpresa().getId())
                .stream()
                .map(this::toResponseConValidacion)
                .collect(Collectors.toList());
    }

    /**
     * Validar integridad de la cadena blockchain.
     */
    @Transactional(readOnly = true)
    public boolean validarIntegridadCadena() {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<AuditoriaEvento> cadena = auditoriaRepository.findCadenaBlockchainByEmpresaId(usuario.getEmpresa().getId());

        for (int i = 0; i < cadena.size(); i++) {
            AuditoriaEvento evento = cadena.get(i);

            // Calcular hash esperado
            String hashCalculado = calcularHash(evento);

            // Verificar hash del evento
            if (!evento.getHashEvento().equals(hashCalculado)) {
                return false;
            }

            // Verificar encadenamiento con evento anterior
            if (i > 0) {
                AuditoriaEvento eventoAnterior = cadena.get(i - 1);
                if (!evento.getHashAnterior().equals(eventoAnterior.getHashEvento())) {
                    return false;
                }
            }
        }

        return true;
    }

    // ========== MÉTODOS PRIVADOS ==========

    private void registrarEvento(User usuario, String tipoEntidad, Long entidadId,
                                 String codigoEntidad, String tipoOperacion, String descripcion,
                                 String datosAnteriores, String datosNuevos, String nivelCriticidad,
                                 boolean enCadena) {

        AuditoriaEvento evento = new AuditoriaEvento();
        evento.setUsuario(usuario);
        evento.setTipoEntidad(tipoEntidad);
        evento.setEntidadId(entidadId);
        evento.setCodigoEntidad(codigoEntidad);
        evento.setTipoOperacion(tipoOperacion);
        evento.setDescripcionOperacion(descripcion);
        evento.setDatosAnteriores(datosAnteriores);
        evento.setDatosNuevos(datosNuevos);
        evento.setEmpresaId(usuario.getEmpresa().getId());
        evento.setEmpresaNombre(usuario.getEmpresa().getRazonSocial());
        evento.setModulo(determinarModulo(tipoEntidad));
        evento.setNivelCriticidad(nivelCriticidad);
        evento.setEnCadena(enCadena);

        // Sí va a la cadena, obtener hash del último evento
        if (enCadena) {
            Optional<AuditoriaEvento> ultimoEvento = auditoriaRepository
                    .findUltimoEventoCadena(usuario.getEmpresa().getId());
            evento.setHashAnterior(ultimoEvento.map(AuditoriaEvento::getHashEvento).orElse("0"));
        }

        // Calcular hash del evento actual
        String hash = calcularHash(evento);
        evento.setHashEvento(hash);

        auditoriaRepository.save(evento);
    }

    private String calcularHash(AuditoriaEvento evento) {
        String datos = String.format(
                "%s|%s|%d|%s|%s|%s|%d|%s",
                evento.getHashAnterior() != null ? evento.getHashAnterior() : "0",
                evento.getUsuario().getId(),
                evento.getEntidadId(),
                evento.getTipoEntidad(),
                evento.getTipoOperacion(),
                evento.getDescripcionOperacion(),
                evento.getEmpresaId(),
                evento.getFechaEvento() != null ? evento.getFechaEvento() : LocalDateTime.now()
        );

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(datos.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al generar hash SHA-256", e);
        }
    }

    private String determinarModulo(String tipoEntidad) {
        if (tipoEntidad.matches("FINCA|LOTE|COSECHA|ACTIVIDAD")) {
            return "PRODUCCION";
        } else if (tipoEntidad.matches("RECEPCION|CLASIFICACION|ETIQUETA|PALLET|CONTROL_CALIDAD")) {
            return "EMPAQUE";
        } else if (tipoEntidad.matches("ENVIO|EVENTO_LOGISTICO|DOCUMENTO")) {
            return "LOGISTICA";
        }
        return "SISTEMA";
    }

    private AuditoriaEventoResponse toResponseConValidacion(AuditoriaEvento evento) {
        AuditoriaEventoResponse response = auditoriaMapper.toResponse(evento);

        // Si está en cadena, validar integridad
        if (evento.estaEncadenado()) {
            String hashCalculado = calcularHash(evento);
            response.setIntegridadVerificada(evento.getHashEvento().equals(hashCalculado));
        }

        return response;
    }
}