import { Component } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { AutenticacaoService } from '../../../core/services/autenticacao.service';

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

  constructor(private autenticacaoService: AutenticacaoService) {}

  entrar(): void {
    if (this.formularioLogin.invalid) {
      return;
    }

    const { email, senha } = this.formularioLogin.getRawValue();

    this.autenticacaoService.login(email!, senha!).subscribe({
      next: (resposta) => {
        sessionStorage.setItem('token', resposta.token);
        console.log('Token armazenado?', !!sessionStorage.getItem('token'));
      },
      error: (erro) => {
        console.error('Falha ao realizar login. Status HTTP:', erro.status);
      },
    });
  }
}
