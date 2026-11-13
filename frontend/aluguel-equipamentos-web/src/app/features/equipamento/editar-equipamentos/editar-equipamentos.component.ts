import { Component, OnInit } from '@angular/core';
import { EquipamentoResponse } from '../../../core/models/equipamento-response';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EquipamentoService } from '../service/equipamento.service';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { AtualizarEquipamentoRequest } from '../../../core/models/atualizar-equipamento-request';
import { CategoriaService } from '../../categoria/service/categoria.service';
import { CategoriaResponse } from '../../../core/models/categoria-response';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-editar-equipamentos',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './editar-equipamentos.component.html',
  styleUrl: './editar-equipamentos.component.scss',
})
export class EditarEquipamentosComponent implements OnInit {
  equipamento!: EquipamentoResponse;
  mensagemErro = '';
  carregando = false;
  id!: number;
  atualizando = false;
  categorias: CategoriaResponse[] = [];
  categoriaAtualInativa = false;

  constructor(
    private route: ActivatedRoute,
    private equipamentoService: EquipamentoService,
    private categoriaService: CategoriaService,
    private router: Router,
  ) {}

  formularioAtualizacao = new FormGroup({
    nome: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(150),
      ],
    }),
    descricao: new FormControl('', {
      nonNullable: true,
      validators: [Validators.minLength(3), Validators.maxLength(500)],
    }),
    valorDiaria: new FormControl<number | null>(null, {
      validators: [
        Validators.required,
        Validators.min(1),
        Validators.max(100000),
      ],
    }),
    categoriaId: new FormControl<number | null>(null, {
      validators: [Validators.required],
    }),
  });

  ngOnInit(): void {
    const idParametro = this.route.snapshot.paramMap.get('id');

    if (!idParametro) {
      this.mensagemErro = 'ID do equipamento não informado.';
      return;
    }

    this.id = Number(idParametro);

    if (!Number.isInteger(this.id) || this.id <= 0) {
      this.mensagemErro = 'ID do equipamento inválido.';
      return;
    }

    this.carregando = true;

    forkJoin({
      equipamento: this.equipamentoService.buscarPorId(this.id),
      categorias: this.categoriaService.listarAtivas(),
    }).subscribe({
      next: (resposta) => {
        this.carregando = false;

        this.equipamento = resposta.equipamento;
        this.categorias = resposta.categorias;

        const categoriaEstaAtiva = this.categorias.some(
          (categoria) => categoria.id === this.equipamento.categoriaId,
        );

        this.categoriaAtualInativa = !categoriaEstaAtiva;

        this.formularioAtualizacao.patchValue({
          nome: this.equipamento.nome,
          descricao: this.equipamento.descricao ?? '',
          valorDiaria: this.equipamento.valorDiaria,
          categoriaId: categoriaEstaAtiva ? this.equipamento.categoriaId : null,
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
          'Não foi possível carregar os dados do equipamento ou as categorias.';
      },
    });
  }

  atualizar() {
    if (this.atualizando) {
      return;
    }

    if (this.formularioAtualizacao.invalid) {
      this.formularioAtualizacao.markAllAsTouched();
      return;
    }

    const dadosCadastro = this.formularioAtualizacao.getRawValue();

    if (
      dadosCadastro.valorDiaria === null ||
      dadosCadastro.categoriaId === null
    ) {
      return;
    }

    const descricao = dadosCadastro.descricao.trim() || null;

    const equipamento: AtualizarEquipamentoRequest = {
      nome: dadosCadastro.nome,
      descricao: descricao,
      valorDiaria: dadosCadastro.valorDiaria,
      categoriaId: dadosCadastro.categoriaId,
    };

    this.mensagemErro = '';
    this.atualizando = true;
    this.equipamentoService.editarEquipamento(this.id, equipamento).subscribe({
      next: (resposta) => {
        this.atualizando = false;

        this.router.navigate(['/equipamentos'], {
          state: {
            mensagemSucesso: 'Equipamento atualizado com sucesso!',
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
          erro.error?.mensagens?.[0] ??
          'Não foi possível editar o equipamento.';
      },
    });
  }
}
