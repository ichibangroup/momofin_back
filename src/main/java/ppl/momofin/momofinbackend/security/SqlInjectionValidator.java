package ppl.momofin.momofinbackend.security;

public interface SqlInjectionValidator {
    boolean containsSqlInjection(String input);
}
