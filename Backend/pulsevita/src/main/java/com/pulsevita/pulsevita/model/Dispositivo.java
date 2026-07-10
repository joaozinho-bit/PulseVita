package com.pulsevita.pulsevita.model;

import jakarta.persistence.*;

@Entity
@Table(name = "dispositivos")
public class Dispositivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_dispositivo", unique = true, nullable = false)
    private String idDispositivo;

    @Column(name = "mac_address", unique = true, nullable = false)
    private String macAddress;

    public Integer getId() { return id; }
    public String getIdDispositivo() { return idDispositivo; }
    public String getMacAddress() { return macAddress; }
}