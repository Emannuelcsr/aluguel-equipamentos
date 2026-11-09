import { Component, OnInit } from '@angular/core';
import { ClienteResponse } from '../../../core/models/cliente-response';
import { ClienteService } from '../service/cliente.service';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AtualizarClienteRequest } from '../../../core/models/atualizar-cliente-request';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { NgxMaskDirective, provideNgxMask } from 'ngx-mask';

@Component({
  selector: 'app-editar-clientes',
  imports: [ReactiveFormsModule, NgxMaskDirective, RouterLink],
  providers: [provideNgxMask()],
  templateUrl: './editar-clientes.component.html',
  styleUrl: './editar-clientes.component.scss',
})
export class EditarClientesComponent implements OnInit {
  cliente!: ClienteResponse;
  mensagemErro = '';
  carregando = false;
  id!: number;
  atualizando = false;

  constructor(
    private route: ActivatedRoute,
    private clienteService: ClienteService,
    private router: Router,
  ) {}

  formularioAtualizacao = new FormGroup({
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
    const idParametro = this.route.snapshot.paramMap.get('id');

    if (idParametro) {
      this.id = Number(idParametro);

      if (!Number.isInteger(this.id) || this.id <= 0) {
        this.mensagemErro = 'ID do cliente inválido.';
        return;
      }

      this.carregando = true;
      this.clienteService.buscarPorId(this.id).subscribe({
        next: (cliente) => {
          this.carregando = false;
          this.cliente = cliente;
          this.formularioAtualizacao.patchValue({
            nomeRazaoSocial: cliente.nomeRazaoSocial,
            nomeFantasia: cliente.nomeFantasia ?? '',
            email: cliente.email,
            telefone: cliente.telefone,
            cep: cliente.cep,
            logradouro: cliente.logradouro,
            numero: cliente.numero,
            complemento: cliente.complemento ?? '',
            bairro: cliente.bairro,
            cidade: cliente.cidade,
            estado: cliente.estado,
          });
        },
        error: (erro) => {
          this.carregando = false;
          if (erro.status === 0) {
            this.mensagemErro =
              'Não foi possível conectar ao servidor. Verifique a conexão e tente novamente.';
            return;
          }

          this.mensagemErro =
            erro.error?.mensagens?.[0] ??
            'Não foi possível carregar os dados do cliente.';
        },
      });
    }
  }
  atualizar() {
    if (this.atualizando) {
      return;
    }
    if (this.formularioAtualizacao.invalid) {
      this.formularioAtualizacao.markAllAsTouched();
      return;
    }

    const dados = this.formularioAtualizacao.getRawValue();

    const nomeFantasia =
      this.cliente.tipo === 'PJ' ? dados.nomeFantasia || null : null;

    const complemento = dados.complemento || null;

    const dadosAtualizacao: AtualizarClienteRequest = {
      ...dados,
      nomeFantasia,
      complemento,
      estado: dados.estado.toUpperCase(),
    };

    this.atualizando = true;
    this.clienteService.editarCliente(this.id, dadosAtualizacao).subscribe({
      next: (clienteAtualizado) => {
        this.atualizando = false;
        this.router.navigate(['/clientes']);
      },

      error: (erro) => {
        this.atualizando = false;
        if (erro.status === 0) {
          this.mensagemErro =
            'Não foi possível conectar ao servidor. Verifique a conexão e tente novamente.';
          return;
        }

        this.mensagemErro =
          erro.error?.mensagens?.[0] ?? 'Não foi possível editar o cliente.';
      },
    });
  }
}
