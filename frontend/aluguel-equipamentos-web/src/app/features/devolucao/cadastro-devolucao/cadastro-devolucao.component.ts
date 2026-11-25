import { Component } from '@angular/core';
import {
  FormArray,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ReservaResponse } from '../../../core/models/reserva-response';
import { CondicaoFisicaRetirada } from '../../../core/models/condicao-fisica-enum';
import { ActivatedRoute, Router } from '@angular/router';
import { ReservaService } from '../../reserva/service/reserva.service';
import { DevolucaoService } from '../service/devolucao.service';
import { CadastrarDevolucaoRequest } from '../../../core/models/cadastrar-devolucao-request';
import { DatePipe } from '@angular/common';
import { CondicaoFisicaDevolucao } from '../../../core/models/condicao-fisica-devolucao.enum';

@Component({
  selector: 'app-cadastro-devolucao',
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './cadastro-devolucao.component.html',
  styleUrl: './cadastro-devolucao.component.scss',
})
export class CadastroDevolucaoComponent {
  mensagemErro = '';
  reserva: ReservaResponse | null = null;
  cadastrando = false;
  mensagemSucesso = '';
  mensagemSucessoAlugada = '';

  constructor(
    private route: ActivatedRoute,
    private reservaService: ReservaService,
    private devolucaoService: DevolucaoService,
    private router: Router,
  ) {}

  formularioCadastro = new FormGroup({
    confirmacaoRecebimento: new FormControl<boolean>(false, {
      nonNullable: true,
      validators: [Validators.requiredTrue],
    }),

    itens: new FormArray<
      ReturnType<CadastroDevolucaoComponent['criarFormularioItem']>
    >([]),
  });

  ngOnInit(): void {
    const reservaId = this.route.snapshot.paramMap.get('reservaId');

    const id = Number(reservaId);

    if (!reservaId || !Number.isInteger(id) || id <= 0) {
      this.mensagemErro = 'ID da reserva inválido.';
      return;
    }

    this.reservaService.buscarPorId(id).subscribe({
      next: (resposta) => {
        this.reserva = resposta;

        resposta.itens.forEach((item) => {
          this.adicionarItem(item.id);
        });
      },
      error: (erro) => {
        this.mensagemErro = 'Não foi possível carregar a reserva.';
      },
    });
  }

  registrarDevolucao(): void {
    if (this.cadastrando || this.mensagemSucesso) {
      return;
    }

    this.mensagemErro = '';

    if (this.formularioCadastro.invalid) {
      this.formularioCadastro.markAllAsTouched();
      this.mensagemErro = 'Preencha corretamente os campos obrigatórios.';
      return;
    }

    const dadosFormulario = this.formularioCadastro.getRawValue();

    if (dadosFormulario.itens.some((item) => item.condicaoFisica === null)) {
      return;
    }

    if (!this.reserva) {
      return;
    }

    const reservaId = this.reserva.id;

    const dadosDevolucao: CadastrarDevolucaoRequest = {
      ...dadosFormulario,
      reservaId: reservaId,
      itens: dadosFormulario.itens
        .filter((item) => item.condicaoFisica !== null)
        .map((item) => ({
          itemReservaId: item.itemReservaId,
          condicaoFisica: item.condicaoFisica!,
          observacoes: item.observacoes,
        })),
    };

    this.cadastrando = true;
    this.devolucaoService.cadastrar(dadosDevolucao).subscribe({
      next: (resposta) => {
        this.cadastrando = false;

        this.router.navigate(['/devolucoes'], {
          state: {
            mensagemSucessoAlugada: 'Devolução realizada com sucesso!',
          },
        });
      },
      error: (erro) => {
        this.cadastrando = false;
        this.mensagemErro =
          erro.error?.mensagens?.[0] ??
          'Não foi possível registrar a devolução.';
      },
    });
  }

  criarFormularioItem(itemReservaId: number) {
    const grupo = new FormGroup({
      itemReservaId: new FormControl(itemReservaId, {
        nonNullable: true,
      }),

      condicaoFisica: new FormControl<CondicaoFisicaDevolucao | null>(null, {
        validators: [Validators.required],
      }),
      observacoes: new FormControl('', {
        nonNullable: true,
        validators: [Validators.maxLength(500)],
      }),
    });

    grupo.controls.condicaoFisica.valueChanges.subscribe((condicao) => {
      if (condicao === CondicaoFisicaDevolucao.COM_OBSERVACOES) {
        grupo.controls.observacoes.setValidators([
          Validators.required,
          Validators.maxLength(500),
        ]);
      } else {
        grupo.controls.observacoes.setValidators([Validators.maxLength(500)]);
      }
      grupo.controls.observacoes.updateValueAndValidity();
    });

    return grupo;
  }

  adicionarItem(itemReservaId: number) {
    this.formularioCadastro.controls.itens.push(
      this.criarFormularioItem(itemReservaId),
    );
  }

  confirmarSaida(): boolean {
    if (this.formularioCadastro.dirty && !this.mensagemSucesso) {
      return confirm('Existem dados não salvos. Deseja realmente sair?');
    }

    return true;
  }

  voltar(): void {
    if (!this.confirmarSaida()) {
      return;
    }

    if (this.reserva) {
      this.router.navigate(['/reservas/detalhes', this.reserva.id]);
    }
  }
}
