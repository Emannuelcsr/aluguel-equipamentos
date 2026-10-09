CREATE SEQUENCE unidades_equipamento_seq
    START WITH 1
    INCREMENT BY 1;

CREATE TABLE unidades_equipamento (
    id BIGINT NOT NULL DEFAULT nextval('unidades_equipamento_seq'),
    codigo VARCHAR(20) NOT NULL,
    equipamento_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_unidades_equipamento PRIMARY KEY (id),
    CONSTRAINT uk_unidades_equipamento_codigo UNIQUE (codigo),
    CONSTRAINT fk_unidades_equipamento_equipamento_id
        FOREIGN KEY (equipamento_id)
        REFERENCES equipamentos(id)
);

ALTER SEQUENCE unidades_equipamento_seq
    OWNED BY unidades_equipamento.id;