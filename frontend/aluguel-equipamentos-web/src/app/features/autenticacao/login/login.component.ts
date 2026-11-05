import { Component } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { AutenticacaoService } from '../../../core/services/autenticacao.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  formularioLogin = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    senha: new FormControl('', [Validators.required]),
  });

  mensagemErro = '';
  autenticando = false;

  constructor(
    private autenticacaoService: AutenticacaoService,
    private router: Router,
  ) {}

  entrar(): void {
    this.mensagemErro = '';

    if (this.formularioLogin.invalid) {
      return;
    }

    const { email, senha } = this.formularioLogin.getRawValue();

    this.autenticando = true;
    this.autenticacaoService.login(email!, senha!).subscribe({
      next: (resposta) => {
        this.autenticando = false;
        sessionStorage.setItem('token', resposta.token);
        sessionStorage.setItem('usuario', JSON.stringify(resposta.usuario));
        this.router.navigate(['/clientes']);
      },
      error: (erro) => {
        this.autenticando = false;
        this.mensagemErro =
          erro.error?.mensagens?.[0] ?? 'Ocorreu um erro inesperado';
      },
    });
  }
}
