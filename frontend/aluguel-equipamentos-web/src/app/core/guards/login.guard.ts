import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AutenticacaoService } from '../services/autenticacao.service';

export const loginGuard: CanActivateFn = (route, state) => {

  const autenticacaoService = inject(AutenticacaoService);
  const router = inject(Router);

  if (autenticacaoService.estaAutenticado()) {
    return router.createUrlTree(['/clientes']);
  }

  return true;
};