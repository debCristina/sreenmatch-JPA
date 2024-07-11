package br.com.debora.screenmatch_com_jpa.service;

public interface IConverteDados {
    <T> T obterDados(String json, Class<T> classe);
}
