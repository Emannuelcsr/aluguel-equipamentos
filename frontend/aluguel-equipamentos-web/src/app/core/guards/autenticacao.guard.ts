import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AutenticacaoService } from '../services/autenticacao.service';

export const autenticacaoGuard: CanActivateFn = (route, state) => {
  const autenticacaoService = inject(AutenticacaoService);
  const router = inject(Router);

  if (autenticacaoService.estaAutenticado()) {
    return true;
  }

  return router.createUrlTree(['/login']);

};
