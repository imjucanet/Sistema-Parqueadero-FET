package com.fet.parqueadero.adapter.out.persistence.entity;

import com.fet.parqueadero.domain.model.TipoUsuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/** Entidad JPA que representa la tabla {@code PROPIETARIO}. */
@Entity
@Table(name = "PROPIETARIO")
public class PropietarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "PRIMER_NOMBRE", nullable = false, length = 80)
    private String primerNombre;

    @Column(name = "PRIMER_APELLIDO", nullable = false, length = 80)
    private String primerApellido;

    @Column(name = "NOMBRE_COMPLETO", nullable = false, length = 165)
    private String nombreCompleto;

    @Column(name = "NUMERO_DOCUMENTO", nullable = false, length = 30, unique = true)
    private String numeroDocumento;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_USUARIO", nullable = false, length = 30)
    private TipoUsuario tipoUsuario;

    @Column(name = "TELEFONO", length = 30)
    private String telefono;

    @Column(name = "CORREO", length = 120)
    private String correo;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPrimerNombre() {
        return primerNombre;
    }

    public void setPrimerNombre(String primerNombre) {
        this.primerNombre = primerNombre;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}
