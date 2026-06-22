Component({
  properties: {
    title: { type: String, value: '' },
    showBack: { type: Boolean, value: true },
    gold: { type: Number, value: -1 },
  },
  methods: {
    onBack() {
      this.triggerEvent('back')
    },
  },
})
