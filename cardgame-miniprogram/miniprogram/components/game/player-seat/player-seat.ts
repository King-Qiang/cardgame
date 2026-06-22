Component({
  properties: {
    nickname: { type: String, value: '' },
    avatarText: { type: String, value: '?' },
    handCount: { type: Number, value: 0 },
    isLandlord: { type: Boolean, value: false },
    isActive: { type: Boolean, value: false },
    align: { type: String, value: 'left' },
    tableCards: { type: Array, value: [] as string[] },
    tableBackCount: { type: Number, value: 0 },
    showPass: { type: Boolean, value: false },
    passLabel: { type: String, value: '' },
  },
  data: {
    tableBacks: [] as number[],
  },
  observers: {
    tableBackCount(count: number) {
      const n = Math.max(0, count || 0)
      this.setData({ tableBacks: Array.from({ length: n }, (_, i) => i) })
    },
  },
})
