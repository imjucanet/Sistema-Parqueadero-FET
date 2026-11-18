package com.fet.parqueadero.adapter.out.persistence.mapper;

import com.fet.parqueadero.adapter.out.persistence.entity.PropietarioEntity;
import com.fet.parqueadero.domain.model.Propietario;

/** Convierte entre la entidad {@code PROPIETARIO} y el modelo de dominio. */
public final class PropietarioEntityMapper {

    private PropietarioEntityMapper() {
    }

    public static PropietarioEntity toEntity(Propietario dominio) {
        PropietarioEntity entidad = new PropietarioEntity();
        entidad.setId(dominio.id());
        entidad.setPrimerNombre(dominio.primerNombre());
        entidad.setPrimerApellido(dominio.primerApellido());
        entidad.setNombreCompleto(dominio.nombreCompleto());
        entidad.setNumeroDocumento(dominio.numeroDocumento());
        entidad.setTipoUsuario(dominio.tipoUsuario());
        entidad.setTelefono(dominio.telefono());
        entidad.setCorreo(dominio.correo());
        return entidad;
    }

    public static Propietario toDomain(PropietarioEntity entidad) {
        return new Propietario(
                entidad.getId(),
                entidad.getPrimerNombre(),
                entidad.getPrimerApellido(),
                entidad.getNombreCompleto(),
                entidad.getNumeroDocumento(),
                entidad.getTipoUsuario(),
                entidad.getTelefono(),
                entidad.getCorreo());
    }
}
