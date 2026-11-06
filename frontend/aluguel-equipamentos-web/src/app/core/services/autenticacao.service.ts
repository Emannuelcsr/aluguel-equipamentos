import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { LoginResponse } from '../models/login-response';
import { jwtDecode } from 'jwt-decode';
import { UsuarioAutenticadoResponse } from '../models/usuario-autenticado-response';
import { API_URL } from '../config/api.config';

@Injectable({
  providedIn: 'root',
})
export class AutenticacaoService {
  constructor(private http: HttpClient) {}

  login(email: string, senha: string) {
    const dadosLogin = {
      email: email,
      senha: senha,
    };

    return this.http.post<LoginResponse>(
      `${API_URL}/autenticacao/login`,
      dadosLogin,
    );
  }

  estaAutenticado(): boolean {
    const token = sessionStorage.getItem('token');

    if (!token) {
      this.logout();
      return false;
    }

    try {
      const dadosToken = jwtDecode(token);

      const agora = Math.floor(Date.now() / 1000);

      if (dadosToken.exp && dadosToken.exp > agora) {
        return true;
      }

      this.logout();
      return false;
    } catch {
      this.logout();
      return false;
    }
  }

  logout(): void {
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('usuario');
  }

  obterUsuario(): UsuarioAutenticadoResponse | null {
    const usuario = sessionStorage.getItem('usuario');

    if (!usuario) {
      return null;
    }

    try {
      return JSON.parse(usuario) as UsuarioAutenticadoResponse;
    } catch {
      return null;
    }
  }
}
