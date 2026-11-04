import { HttpInterceptorFn } from '@angular/common/http';

export const autenticacaoInterceptor: HttpInterceptorFn = (req, next) => {
  const token = sessionStorage.getItem('token');

  const urlApi = 'http://localhost:8080/api/';

  const urlLogin = 'http://localhost:8080/api/autenticacao/login';

  if (token && req.url.startsWith(urlApi) && req.url !== urlLogin) {
    const requisicaoAutenticada = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });

    return next(requisicaoAutenticada);
  }

  return next(req);
};
