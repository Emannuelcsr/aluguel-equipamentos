import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { LoginResponse } from '../models/login-response';

@Injectable({
  providedIn: 'root'
})
export class AutenticacaoService {

  constructor(private http: HttpClient) { }

login(email:string, senha:string){

  const dadosLogin = {
    email: email,
    senha: senha
  };

  return this.http.post<LoginResponse>('http://localhost:8080/api/autenticacao/login',dadosLogin);
}

}
