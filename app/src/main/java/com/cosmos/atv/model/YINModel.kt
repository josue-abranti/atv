import utils.Constants

class YINModel() {
        // Define o limiar minimo para considerar um valor no calculo da frequencia fundamental.
        private val threshold = Constants.THRESHOLD
        // Define o limiar minimo de probabilidade para aceitar uma estimativa de frequencia fundamental.
        private val probabilityThreshold = Constants.PROBABILITY_THRESHOLD
        // Variavel que armazena o valor de tau, que e usado no calculo da frequencia fundamental.
        private var tau: Int = 0

        //  Funcao responsavel por calcular a frequencia fundamental do sinal de audio.
        fun detectPitch(audioBuffer: ShortArray, sampleRate: Int, bufferSize: Int): Double {
            // Cria um array para armazenar os valores do buffer YIN, que sera usado no calculo da frequencia fundamental.
            val yinBuffer = DoubleArray(bufferSize / 2)
            // Converte o buffer de audio de ShortArray para DoubleArray, normalizando os valores para o intervalo de -1.0 a 1.0.
            val floatBuffer = audioBuffer.map { it.toDouble() / Short.MAX_VALUE }.toDoubleArray()

            // Calculo da metade do tamanho do buffer
            val bufferSizeDiv2 = bufferSize / 2
            val bufferSizeDiv2MinusTau = bufferSizeDiv2 - tau

            // Calcula a funcao de diferenca do YIN, conforme descrito no passo 2 do artigo do YIN.
            // Ele consiste em dois loops aninhados que realizam a soma dos quadrados das diferencas entre os
            // valores do buffer de audio em diferentes deslocamentos t
            for (t in 0 until bufferSizeDiv2) {
                yinBuffer[t] = 0.0
                for (j in 0 until bufferSizeDiv2MinusTau) {
                    val delta = floatBuffer[j] - floatBuffer[j + t]
                    yinBuffer[t] += (delta * delta)
                }
            }

            // Calculo da diferenca acumulada normalizada, conforme descrito no passo 3 do artigo.
            // Primeiro, e inicializado o valor de yinBuffer[0] como 1.0. Em seguida, e realizado um loop
            // para calcular a diferenca acumulada normalizada para cada valor de t ate bufferSizeDiv2.
            yinBuffer[0] = 1.0
            var runningSum = 0.0
            for (t in 1 until bufferSizeDiv2) {
                runningSum += yinBuffer[t]
                yinBuffer[t] *= t.toDouble() / runningSum
            }

            // Calculo do limiar absoluto onde tau como 2 e continua incrementando ate encontrar um
            // valor em yinBuffer menor que o limiar definido (threshold).
            tau = 2
            while (tau < bufferSizeDiv2 && yinBuffer[tau] >= threshold) {
                tau++
            }

            // Verificacao se nenhum tom foi detectado, neste caso, se nenhum tom for encontrado
            // (tau igual a bufferSizeDiv2 ou o valor em yinBuffer[tau] for maior ou igual ao limiar),
            // retorna 0.0.
            if (tau == bufferSizeDiv2 || yinBuffer[tau] >= threshold) {
                return 0.0
            }

            // Calculo da probabilidade chamando a funcao calculateProbability para calcular a
            // probabilidade com base nos dados em yinBuffer.
            val probability = calculateProbability(yinBuffer)

            // Verificacao de probabilidade baixa para caso a probabilidade calculada for menor que
            // o limiar definido (probabilityThreshold), retorna 0.0.
            if (probability < probabilityThreshold) {
                return 0.0
            }

            // Refinamento do valor de tau com interpolacao parabolica: Implementa o passo 5 do artigo
            // do YIN, refinando o valor estimado de tau usando interpolacao parabolica.
            val betterTau: Int = if (tau < 1) {
                tau
            } else if (tau + 1 < bufferSizeDiv2) {
                val s0 = yinBuffer[tau - 1]
                val s1 = yinBuffer[tau]
                val s2 = yinBuffer[tau + 1]
                tau + ((s2 - s0) / (2 * (2 * s1 - s2 - s0))).toInt()
            } else {
                tau
            }

            // Retorna a frequencia fundamental estimada dividindo a taxa de amostragem pelo valor
            // refinado de tau e a retorna como resultado da funcao
            return sampleRate.toDouble() / betterTau
        }

        // Funcao auxiliar para calcular a probabilidade com base nos valores em yinBuffer.
        // Retorna a media dos valores em yinBuffer.
        private fun calculateProbability(yinBuffer: DoubleArray): Double {
            return yinBuffer.average()
        }
    }