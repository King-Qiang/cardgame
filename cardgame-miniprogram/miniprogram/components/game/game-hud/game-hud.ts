Component({
  properties: {
    roomId: { type: String, value: '' },
    multiplier: { type: Number, value: 1 },
    turnSeconds: { type: Number, value: 30 },
    connected: { type: Boolean, value: false },
    urgent: { type: Boolean, value: false },
  },
  methods: {
    onExit() {
      this.triggerEvent('exit')
    },
  },
})
