Component({
  properties: {
    phase: { type: String, value: 'BIDDING' },
    myTurn: { type: Boolean, value: false },
    canPass: { type: Boolean, value: false },
    canPlay: { type: Boolean, value: false },
    loading: { type: Boolean, value: false },
  },
  methods: {
    onPass() {
      this.triggerEvent('pass')
    },
    onCall() {
      this.triggerEvent('call')
    },
    onPlay() {
      this.triggerEvent('play')
    },
  },
})
