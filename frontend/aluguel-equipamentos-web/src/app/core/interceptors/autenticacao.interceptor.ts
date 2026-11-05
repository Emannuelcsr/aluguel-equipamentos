import { HttpInterceptorFn } from '@angular/common/http';
import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AutenticacaoService } from '../services/autenticacao.service';

export const autenticacaoInterceptor: HttpInterceptorFn = (req, next) => {
  const token = sessionStorage.getItem('token');

  const urlApi = 'http://localhost:8080/api/';

  const urlLogin = 'http://localhost:8080/api/autenticacao/login';

  const router = inject(Router);

  const autenticacaoService = inject(AutenticacaoService);

  if (token && req.url.startsWith(urlApi) && req.url !== urlLogin) {
    const requisicaoAutenticada = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });

    return next(requisicaoAutenticada).pipe(
      catchError((erro: HttpErrorResponse) => {
        if (erro.status === 401) {
          autenticacaoService.logout();
          router.navigate(['/login']);
        }

        return throwError(() => erro);
      }),
    );
  }

  return next(req);
};
