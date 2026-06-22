Component({
  properties: {
    faceCards: { type: Array, value: [] as string[] },
    backCount: { type: Number, value: 0 },
    phaseLabel: { type: String, value: '' },
    subtitle: { type: String, value: '' },
  },
  data: {
    backs: [] as number[],
    faces: [] as { code: string }[],
  },
  observers: {
    'faceCards, backCount'(faceCards: string[], backCount: number) {
      const faces = (faceCards || []).map((code) => ({ code }))
      const n = Math.max(0, backCount || 0)
      this.setData({
        faces,
        backs: Array.from({ length: n }, (_, i) => i),
      })
    },
  },
})
