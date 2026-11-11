import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CategoriaService } from '../service/categoria.service';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { CategoriaResponse } from '../../../core/models/categoria-response';
import { AtualizarCategoriaRequest } from '../../../core/models/atualizar-categoria-request';

@Component({
  selector: 'app-editar-categorias',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './editar-categorias.component.html',
  styleUrl: './editar-categorias.component.scss',
})
export class EditarCategoriasComponent implements OnInit {
  categoria!: CategoriaResponse;
  mensagemErro = '';
  carregando = false;
  id!: number;
  atualizando = false;

  constructor(
    private route: ActivatedRoute,
    private categoriaService: CategoriaService,
    private router: Router,
  ) {}

  formularioAtualizacao = new FormGroup({
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

  ngOnInit(): void {
    const idParametro = this.route.snapshot.paramMap.get('id');

    if (idParametro) {
      this.id = Number(idParametro);

      if (!Number.isInteger(this.id) || this.id <= 0) {
        this.mensagemErro = 'ID da categoria inválido.';
        return;
      }

      this.carregando = true;

      this.categoriaService.buscarPorId(this.id).subscribe({
        next: (categoria) => {
          this.carregando = false;
          this.categoria = categoria;
          this.formularioAtualizacao.patchValue({
            nome: categoria.nome,
            descricao: categoria.descricao ?? '',
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
            'Não foi possível carregar os dados da categoria.';
        },
      });
    } else {
      this.mensagemErro = 'ID da categoria não informado.';
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

    const descricao = dados.descricao.trim() || null;

    const dadosAtualizacao: AtualizarCategoriaRequest = {
      ...dados,
      descricao,
    };

    this.atualizando = true;
    this.mensagemErro = '';
    this.categoriaService.editarCategoria(this.id, dadosAtualizacao).subscribe({
      next: (resposta) => {
        this.atualizando = false;

        this.router.navigate(['/categorias'], {
          state: {
            mensagemSucesso: 'Categoria atualizada com sucesso!',
          },
        });
      },

      error: (erro) => {
        this.atualizando = false;
        if (erro.status === 0) {
          this.mensagemErro =
            'Não foi possível conectar ao servidor. Verifique a conexão e tente novamente.';
          return;
        }

        this.mensagemErro =
          erro.error?.mensagens?.[0] ?? 'Não foi possível editar a categoria.';
      },
    });
  }
}
