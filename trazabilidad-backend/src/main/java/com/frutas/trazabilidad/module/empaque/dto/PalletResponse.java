package com.frutas.trazabilidad.module.empaque.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para Pallet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PalletResponse {

    private Long id;
    private String codigoPallet;
    private LocalDate fechaPaletizado;
    private String tipoPallet;
    private Integer numeroCajas;
    private Double pesoNetoTotal;
    private Double pesoBrutoTotal;
    private Double alturaPallet;
    private String tipoFruta;
    private String calidad;
    private String destino;
    private Double temperaturaAlmacenamiento;
    private String responsablePaletizado;
    private String estadoPallet;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Información adicional
    private Integer totalEtiquetas;
    private List<String> etiquetasCodigos;
}