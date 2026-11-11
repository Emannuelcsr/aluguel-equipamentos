import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CategoriaService } from '../service/categoria.service';
import { CadastrarCategoriaRequest } from '../../../core/models/cadastrar-categoria-request';
import {  provideNgxMask } from 'ngx-mask';


@Component({
  selector: 'app-categoria',
    imports: [ReactiveFormsModule, RouterLink],
  providers: [provideNgxMask()],
  templateUrl: './categoria.component.html',
  styleUrl: './categoria.component.scss',
})
export class CadastroCategoriaComponent {
  mensagemErro = '';
  mensagemSucesso = '';
  criando = false;

  constructor(
    private categoriaService: CategoriaService,
    private router: Router,
  ) {}

  formularioCadastro = new FormGroup({
    nome: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(100),
      ],
    }),
    descricao: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(255)],
    }),
  });

  cadastrar() {
    if (this.criando) {
      return;
    }

    if (this.formularioCadastro.invalid) {
      this.formularioCadastro.markAllAsTouched();
      return;
    }

    const dadosCadastro = this.formularioCadastro.getRawValue();

    const descricao = dadosCadastro.descricao || null;

    const categoria: CadastrarCategoriaRequest = {
      ...dadosCadastro,
      descricao,
    };
    this.mensagemErro = '';
    this.criando = true;
    this.categoriaService.cadastrar(categoria).subscribe({
      next:(resposta)=>{
         this.criando = false;

        this.router.navigate(['/categorias'], {
          state: {
            mensagemSucesso: 'Categoria cadastrada com sucesso!',
          },
        });

      },
      error:(erro)=>{
        this.criando = false;
        if (erro.status === 0) {
          this.mensagemErro =
            'Não foi possível conectar ao servidor. Verifique a conexão e tente novamente.';
          return;
        }
        this.mensagemErro =
          erro.error?.mensagens?.[0] ?? 'Não foi possível cadastrar a categoria.';
      },
      
    });
  }
}

