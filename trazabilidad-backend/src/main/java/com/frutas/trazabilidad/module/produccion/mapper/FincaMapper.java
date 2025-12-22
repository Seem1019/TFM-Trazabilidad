package com.frutas.trazabilidad.module.produccion.mapper;

import com.frutas.trazabilidad.module.produccion.dto.FincaRequest;
import com.frutas.trazabilidad.module.produccion.dto.FincaResponse;
import com.frutas.trazabilidad.module.produccion.entity.Finca;
import org.springframework.stereotype.Component;

/**
 * Mapper para conversión entre Finca entity y DTOs.
 */
@Component
public class FincaMapper {

    /**
     * Convierte FincaRequest a Finca entity.
     */
    public Finca toEntity(FincaRequest request) {
        return Finca.builder()
                .codigoFinca(request.getCodigoFinca())
                .nombre(request.getNombre())
                .ubicacion(request.getUbicacion())
                .municipio(request.getMunicipio())
                .departamento(request.getDepartamento())
                .pais(request.getPais())
                .areaHectareas(request.getAreaHectareas())
                .propietario(request.getPropietario())
                .encargado(request.getEncargado())
                .telefono(request.getTelefono())
                .email(request.getEmail())
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .observaciones(request.getObservaciones())
                .build();
    }

    /**
     * Convierte Finca entity a FincaResponse.
     */
    public FincaResponse toResponse(Finca finca) {
        return FincaResponse.builder()
                .id(finca.getId())
                .empresaId(finca.getEmpresa().getId())
                .empresaNombre(finca.getEmpresa().getRazonSocial())
                .codigoFinca(finca.getCodigoFinca())
                .nombre(finca.getNombre())
                .ubicacion(finca.getUbicacion())
                .municipio(finca.getMunicipio())
                .departamento(finca.getDepartamento())
                .pais(finca.getPais())
                .areaHectareas(finca.getAreaHectareas())
                .propietario(finca.getPropietario())
                .encargado(finca.getEncargado())
                .telefono(finca.getTelefono())
                .email(finca.getEmail())
                .latitud(finca.getLatitud())
                .longitud(finca.getLongitud())
                .observaciones(finca.getObservaciones())
                .activo(finca.getActivo())
                .createdAt(finca.getCreatedAt())
                .updatedAt(finca.getUpdatedAt())
                .build();
    }

    /**
     * Actualiza una entidad Finca desde un FincaRequest.
     */
    public void updateEntityFromRequest(FincaRequest request, Finca finca) {
        finca.setCodigoFinca(request.getCodigoFinca());
        finca.setNombre(request.getNombre());
        finca.setUbicacion(request.getUbicacion());
        finca.setMunicipio(request.getMunicipio());
        finca.setDepartamento(request.getDepartamento());
        finca.setPais(request.getPais());
        finca.setAreaHectareas(request.getAreaHectareas());
        finca.setPropietario(request.getPropietario());
        finca.setEncargado(request.getEncargado());
        finca.setTelefono(request.getTelefono());
        finca.setEmail(request.getEmail());
        finca.setLatitud(request.getLatitud());
        finca.setLongitud(request.getLongitud());
        finca.setObservaciones(request.getObservaciones());
    }
}