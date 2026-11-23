package com.vendelote.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sesion_activa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SesionActiva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime ultimoRefresh;

    @Column(length = 50)
    private String ip;

    @Column(length = 500, name = "user_agent")
    private String userAgent;

    @Column(nullable = false)
    private boolean activa = true;

    @Column(nullable = false)
    private LocalDateTime fechaLogueo;
}
