package com.campuslab.audit.repository;

import com.campuslab.audit.model.SolicitudInformacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitudInformacionRepository extends JpaRepository<SolicitudInformacion, Long> {

    Page<SolicitudInformacion> findBySolicitanteSub(String solicitanteSub, Pageable pageable);

    Page<SolicitudInformacion> findByEstado(SolicitudInformacion.Estado estado, Pageable pageable);
}
