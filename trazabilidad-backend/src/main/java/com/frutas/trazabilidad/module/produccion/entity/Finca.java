package com.frutas.trazabilidad.module.produccion.entity;

import com.frutas.trazabilidad.entity.Empresa;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una finca productora de frutas.
 * Cada finca pertenece a una empresa (multiempresa).
 */
@Entity
@Table(name = "fincas", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"codigo_finca", "empresa_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Finca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "codigo_finca", nullable = false, length = 50)
    private String codigoFinca;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 500)
    private String ubicacion;

    @Column(length = 200)
    private String municipio;

    @Column(length = 100)
    private String departamento;

    @Column(length = 50)
    private String pais;

    @Column(name = "area_hectareas")
    private Double areaHectareas;

    @Column(length = 100)
    private String propietario;

    @Column(length = 100)
    private String encargado;

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relaciones

    @OneToMany(mappedBy = "finca", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Certificacion> certificaciones = new ArrayList<>();

    @OneToMany(mappedBy = "finca", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Lote> lotes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Métodos de ayuda

    public void addCertificacion(Certificacion certificacion) {
        certificaciones.add(certificacion);
        certificacion.setFinca(this);
    }

    public void removeCertificacion(Certificacion certificacion) {
        certificaciones.remove(certificacion);
        certificacion.setFinca(null);
    }
}