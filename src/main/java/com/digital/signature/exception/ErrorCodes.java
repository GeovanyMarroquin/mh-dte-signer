package com.digital.signature.exception;

public final class ErrorCodes {

    private ErrorCodes() {
    }

    public static final String CERT_NOT_FOUND = "801";
    public static final String INVALID = "802";
    public static final String NO_PUBLIC_KEY = "803";
    public static final String UNCATALOGUED = "804";
    public static final String REQUIRED_DATA = "809";
    public static final String JSON_TO_STRING = "810";
    public static final String STRING_TO_JSON = "811";
    public static final String NO_FILE = "812";
    public static final String INVALID_PASSWORD = "PasswordInvalido";

    public static final String MSG_CERT_NOT_FOUND = "No existe certificado activo";
    public static final String MSG_INVALID = "No valido";
    public static final String MSG_NO_PUBLIC_KEY = "No existe llave publica para este nit";
    public static final String MSG_UNCATALOGUED = "Error no catalogado";
    public static final String MSG_REQUIRED_DATA = "Son datos requeridos";
    public static final String MSG_INVALID_TAXPAYER_ID = "Formato de NIT no valido - (00000000000000)";
    public static final String MSG_JSON_TO_STRING = "Problemas al convertir Json a String";
    public static final String MSG_STRING_TO_JSON = "Problemas al convertir String a Json";
    public static final String MSG_NO_FILE = "No se encontro el archivo";
}
