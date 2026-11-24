package com.pfc.thindesk.usuarios;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    private String id;

    private String username;

    private String senha;

    private String nome;

    private Perfil perfil;

    private boolean ativo = true;
}
