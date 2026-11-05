import { Component } from '@angular/core';
import { ClienteService } from '../service/cliente.service';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { CadastrarClienteRequest } from '../../../core/models/cadastrar-cliente-request';
import { ClienteResponse } from '../../../core/models/cliente-response';
import { Router } from '@angular/router';
import { NgxMaskDirective, provideNgxMask } from 'ngx-mask';

@Component({
  selector: 'app-cadastro-cliente',
  imports: [ReactiveFormsModule, NgxMaskDirective],
  providers: [provideNgxMask()],
  templateUrl: './cadastro-cliente.component.html',
  styleUrl: './cadastro-cliente.component.scss',
})
export class CadastroClienteComponent {
  mensagemErro = '';
  mensagemSucesso = '';
  criando = false;

  constructor(
    private clienteService: ClienteService,
    private router: Router,
  ) {}

  formularioCadastro = new FormGroup({
    tipo: new FormControl<'PF' | 'PJ' | ''>('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    nomeRazaoSocial: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(150),
      ],
    }),
    nomeFantasia: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(150)],
    }),
    documento: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.minLength(11),
        Validators.maxLength(14),
      ],
    }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.email,
        Validators.maxLength(150),
      ],
    }),
    telefone: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^\d{10,11}$/)],
    }),
    cep: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^\d{8}$/)],
    }),
    logradouro: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(150)],
    }),
    numero: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(20)],
    }),
    complemento: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(100)],
    }),
    bairro: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(100)],
    }),
    cidade: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(100)],
    }),
    estado: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^[A-Za-z]{2}$/)],
    }),
  });

  ngOnInit(): void {
    this.formularioCadastro.controls.tipo.valueChanges.subscribe((tipo) => {
      if (tipo === 'PF') {
        this.formularioCadastro.controls.documento.setValidators([
          Validators.required,
          Validators.minLength(11),
          Validators.maxLength(11),
          Validators.pattern(/^\d{11}$/),
        ]);
      }

      if (tipo === 'PJ') {
        this.formularioCadastro.controls.documento.setValidators([
          Validators.required,
          Validators.minLength(14),
          Validators.maxLength(14),
          Validators.pattern(/^\d{14}$/),
        ]);
      }

      this.formularioCadastro.controls.documento.updateValueAndValidity();
    });
  }

  cadastrar() {
    if (this.criando) {
      return;
    }

    if (this.formularioCadastro.invalid) {
      this.formularioCadastro.markAllAsTouched();
      return;
    }

    const dadosCadastro = this.formularioCadastro.getRawValue();
  console.log('Dados do formulário:', dadosCadastro);

    if (dadosCadastro.tipo === '') {
      return;
    }

    const nomeFantasia =
      dadosCadastro.tipo === 'PJ' ? dadosCadastro.nomeFantasia || null : null;

    const complemento = dadosCadastro.complemento || null;
    const cliente: CadastrarClienteRequest = {
      ...dadosCadastro,
      tipo: dadosCadastro.tipo,
      nomeFantasia,
      complemento,
      estado: dadosCadastro.estado.toUpperCase(),
    };
    this.mensagemErro = '';
    this.criando = true;
    this.clienteService.cadastrar(cliente).subscribe({
      next: (resposta) => {
        this.criando = false;

        this.router.navigate(['/clientes'], {
          state: {
            mensagemSucesso: 'Cliente cadastrado com sucesso!',
          },
        });
      },
      error: (erro) => {
        this.criando = false;
        this.mensagemErro =
          erro.error?.mensagens?.[0] ?? 'Ocorreu um erro inesperado';
      },
    });
  }
}
