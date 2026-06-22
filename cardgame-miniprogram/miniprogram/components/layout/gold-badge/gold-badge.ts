Component({
  properties: {
    gold: {
      type: Number,
      value: 0,
    },
  },
  data: {
    goldText: '0',
  },
  observers: {
    gold(value: number) {
      this.setData({ goldText: (value ?? 0).toLocaleString('zh-CN') })
    },
  },
  methods: {
    onTap() {
      this.triggerEvent('tap')
    },
  },
})
