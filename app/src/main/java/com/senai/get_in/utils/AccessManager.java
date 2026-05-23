package com.senai.get_in.utils;

import com.senai.get_in.R;
import com.senai.get_in.model.UsuarioDetalhado;

public class AccessManager {

    public static boolean isAdmin(UsuarioDetalhado user) {
        String cargo = getCargo(user);
        return cargo.equals("adm") || cargo.equals("administrador");
    }

    public static boolean isGerente(UsuarioDetalhado user) {
        String cargo = getCargo(user);
        return cargo.equals("ger") || cargo.equals("gerente");
    }

    public static boolean isSupervisor(UsuarioDetalhado user) {
        String cargo = getCargo(user);
        return cargo.equals("sup") || cargo.equals("supervisor");
    }

    public static boolean isPortaria(UsuarioDetalhado user) {
        String cargo = getCargo(user);
        return cargo.equals("port") || cargo.equals("portaria") || cargo.equals("porteiro");
    }

    public static boolean isFuncionario(UsuarioDetalhado user) {
        String cargo = getCargo(user);
        return cargo.equals("func") || cargo.equals("funcionario") || cargo.isEmpty();
    }

    public static String getCargo(UsuarioDetalhado user) {
        if (user == null || user.getCargo() == null) return "";
        return user.getCargo().trim().toLowerCase();
    }

    public static int getStartDestinationId(UsuarioDetalhado user) {
        if (isPortaria(user)) return R.id.nav_checkIn;
        if (isAdmin(user) || isGerente(user)) return R.id.nav_monitoramento;
        return R.id.nav_perfil;
    }

    public static boolean isAllowedDestination(UsuarioDetalhado user, int destinationId) {
        if (destinationId == R.id.nav_perfil || 
            destinationId == R.id.nav_notificacoes ||
            destinationId == R.id.menu_configuracoes ||
            destinationId == R.id.nav_usuario_detalhado) return true;

        if (isAdmin(user)) return true;
        
        if (isGerente(user)) {
            return destinationId != R.id.nav_checkIn;
        }
        
        if (isSupervisor(user)) {
            return destinationId == R.id.nav_monitoramento;
        }
        
        if (isPortaria(user)) {
            return destinationId == R.id.nav_checkIn || 
                   destinationId == R.id.nav_monitoramento;
        }
        
        return false;
    }
}
