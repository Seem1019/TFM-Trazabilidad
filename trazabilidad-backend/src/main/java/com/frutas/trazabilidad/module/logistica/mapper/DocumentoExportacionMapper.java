package com.frutas.trazabilidad.module.logistica.mapper;

import com.frutas.trazabilidad.module.logistica.dto.DocumentoExportacionRequest;
import com.frutas.trazabilidad.module.logistica.dto.DocumentoExportacionResponse;
import com.frutas.trazabilidad.module.logistica.entity.DocumentoExportacion;
import com.frutas.trazabilidad.module.logistica.entity.Envio;
import org.springframework.stereotype.Component;

@Component
public class DocumentoExportacionMapper {

    public DocumentoExportacion toEntity(DocumentoExportacionRequest request, Envio envio) {
        DocumentoExportacion doc = new DocumentoExportacion();
        doc.setEnvio(envio);
        doc.setTipoDocumento(request.getTipoDocumento());
        doc.setNumeroDocumento(request.getNumeroDocumento());
        doc.setFechaEmision(request.getFechaEmision());
        doc.setFechaVencimiento(request.getFechaVencimiento());
        doc.setEntidadEmisora(request.getEntidadEmisora());
        doc.setFuncionarioEmisor(request.getFuncionarioEmisor());
        doc.setUrlArchivo(request.getUrlArchivo());
        doc.setTipoArchivo(request.getTipoArchivo());
        doc.setTamanoArchivo(request.getTamanoArchivo());
        doc.setHashArchivo(request.getHashArchivo());
        doc.setDescripcion(request.getDescripcion());
        doc.setValorDeclarado(request.getValorDeclarado());
        doc.setMoneda(request.getMoneda());
        doc.setObligatorio(request.getObligatorio());
        doc.setEstado("GENERADO");
        doc.setActivo(true);
        return doc;
    }

    public void updateEntity(DocumentoExportacion doc, DocumentoExportacionRequest request) {
        doc.setTipoDocumento(request.getTipoDocumento());
        doc.setNumeroDocumento(request.getNumeroDocumento());
        doc.setFechaEmision(request.getFechaEmision());
        doc.setFechaVencimiento(request.getFechaVencimiento());
        doc.setEntidadEmisora(request.getEntidadEmisora());
        doc.setFuncionarioEmisor(request.getFuncionarioEmisor());
        doc.setUrlArchivo(request.getUrlArchivo());
        doc.setTipoArchivo(request.getTipoArchivo());
        doc.setTamanoArchivo(request.getTamanoArchivo());
        doc.setHashArchivo(request.getHashArchivo());
        doc.setDescripcion(request.getDescripcion());
        doc.setValorDeclarado(request.getValorDeclarado());
        doc.setMoneda(request.getMoneda());
        doc.setObligatorio(request.getObligatorio());
    }

    public DocumentoExportacionResponse toResponse(DocumentoExportacion doc) {
        return DocumentoExportacionResponse.builder()
                .id(doc.getId())
                .envioId(doc.getEnvio().getId())
                .codigoEnvio(doc.getEnvio().getCodigoEnvio())
                .tipoDocumento(doc.getTipoDocumento())
                .numeroDocumento(doc.getNumeroDocumento())
                .fechaEmision(doc.getFechaEmision())
                .fechaVencimiento(doc.getFechaVencimiento())
                .estaVencido(doc.estaVencido())
                .entidadEmisora(doc.getEntidadEmisora())
                .funcionarioEmisor(doc.getFuncionarioEmisor())
                .urlArchivo(doc.getUrlArchivo())
                .tipoArchivo(doc.getTipoArchivo())
                .tamanoArchivo(doc.getTamanoArchivo())
                .hashArchivo(doc.getHashArchivo())
                .tieneArchivo(doc.tieneArchivo())
                .estado(doc.getEstado())
                .estaAprobado(doc.estaAprobado())
                .descripcion(doc.getDescripcion())
                .valorDeclarado(doc.getValorDeclarado())
                .moneda(doc.getMoneda())
                .obligatorio(doc.getObligatorio())
                .activo(doc.getActivo())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }
}