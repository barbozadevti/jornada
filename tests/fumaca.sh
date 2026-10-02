#!/usr/bin/env bash
# Teste de fumaça contra um servidor já no ar: login de verdade, papéis e CSRF.
# Espera um servidor recém-iniciado (com os dados de exemplo): a trilha do João tem uma mentoria futura que trava a repetição.
# Uso: bash tests/fumaca.sh [url-base]
set -euo pipefail
BASE="${1:-http://localhost:5270}"
JAR=$(mktemp)
trap 'rm -f "$JAR"' EXIT

ok() { echo "ok    $1"; }
falha() { echo "FALHA $1"; exit 1; }
token() { grep XSRF-TOKEN "$JAR" | awk '{print $7}' | tail -1; }
http() { curl -s -o /dev/null -w '%{http_code}' "$@"; }

curl -sf -c "$JAR" -b "$JAR" "$BASE/health" > /dev/null && ok "servidor no ar"
[ "$(http "$BASE/api/bootcamps")" = 401 ] && ok "sem login a API responde 401" || falha "API aberta sem login"
curl -sf "$BASE/api/certificados/JRN-D1-B3" | grep -q "Fundamentos de Web" && ok "certificado é público" || falha "certificado"

curl -sf -c "$JAR" -b "$JAR" "$BASE/api/auth/csrf" > /dev/null
[ "$(http -b "$JAR" -c "$JAR" -X POST -H 'Content-Type: application/json' -d '{"email":"joao@jornada.dev","senha":"Jornada@2026"}' "$BASE/api/auth/entrar")" = 403 ] \
  && ok "POST sem token CSRF é recusado" || falha "CSRF"

entrar() {
  curl -sf -c "$JAR" -b "$JAR" "$BASE/api/auth/csrf" > /dev/null
  curl -sf -c "$JAR" -b "$JAR" -X POST -H "X-XSRF-TOKEN: $(token)" -H 'Content-Type: application/json' \
    -d "{\"email\":\"$1\",\"senha\":\"$2\"}" "$BASE/api/auth/entrar"
}
post() { curl -s -o /dev/null -w '%{http_code}' -c "$JAR" -b "$JAR" -X POST -H "X-XSRF-TOKEN: $(token)" -H 'Content-Type: application/json' -d "$2" "$BASE$1"; }

entrar joao@jornada.dev Jornada@2026 | grep -q '"papel":"ALUNO"' && ok "aluno entra" || falha "login do aluno"
curl -sf -b "$JAR" "$BASE/api/devs" | grep -q "Camila" && falha "aluno vê outro dev" || ok "aluno vê só a si mesmo"
[ "$(http -b "$JAR" "$BASE/api/devs/d1")" = 404 ] && ok "jornada de outro aluno responde 404" || falha "acesso a outro dev"
[ "$(post /api/bootcamps '{"nome":"X","dataInicial":"2030-01-01","duracaoEmDias":30,"vagas":5}')" = 403 ] && ok "aluno não cria bootcamp" || falha "aluno criou bootcamp"
curl -sf -c "$JAR" -b "$JAR" -X POST -H "X-XSRF-TOKEN: $(token)" "$BASE/api/devs/d2/matriculas/b1/progresso" | grep -q '"xpGanho"' \
  && ok "aluno conclui um conteúdo na própria jornada" || falha "progresso do aluno"

entrar coordenador@jornada.dev Jornada@2026 | grep -q '"papel":"COORDENADOR"' && ok "coordenador entra" || falha "login do coordenador"
curl -sf -b "$JAR" "$BASE/api/devs" | grep -q "Camila" && ok "coordenador vê todos os devs" || falha "lista do coordenador"
N="${FUMACA_N:-$RANDOM}"
[ "$(post /api/devs "{\"nome\":\"Aluno $N\",\"email\":\"aluno$N@jornada.dev\",\"senha\":\"senha-do-aluno\"}")" = 201 ] && ok "coordenador cadastra aluno com senha" || falha "cadastro"
entrar "aluno$N@jornada.dev" senha-do-aluno | grep -q "\"nome\":\"Aluno $N\"" && ok "o aluno novo já entra" || falha "login do aluno novo"
echo; echo "Fumaça concluída."
