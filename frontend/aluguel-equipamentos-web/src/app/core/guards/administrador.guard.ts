import { CanActivateFn, Router } from '@angular/router';
import { AutenticacaoService } from '../services/autenticacao.service';
import { inject } from '@angular/core';

export const administradorGuard: CanActivateFn = (route, state) => {
  const autenticacaoService = inject(AutenticacaoService);
  const router = inject(Router);
  const perfil = autenticacaoService.obterUsuario()?.perfil;

  if (perfil === 'ADMINISTRADOR') {
    return true;
  }

  if (perfil === 'FUNCIONARIO') {
    return router.createUrlTree(['/clientes']);
  }

  return router.createUrlTree(['/login']);
};
