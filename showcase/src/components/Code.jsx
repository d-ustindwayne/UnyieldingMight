import { Box } from '@mui/material';
import { ember, mono } from '../theme';

// Minimal Kotlin tokenizer: enough for short excerpts, no highlighter dependency.
const TOKEN = /(\/\/.*$|\/\*.*?\*\/)|("(?:[^"\\]|\\.)*")|\b(fun|val|var|if|else|when|return|try|catch|interface|suspend|class|object)\b|\b([A-Z][A-Za-z0-9_]*)\b|\b(\d+)\b/gm;

const colors = { comment: '#6B6B6B', string: '#8FD3A8', keyword: ember, type: '#E8C7A0', number: '#9CC4FF' };

function highlight(code) {
  const out = [];
  let last = 0;
  let k = 0;
  for (const m of code.matchAll(TOKEN)) {
    if (m.index > last) out.push(code.slice(last, m.index));
    const kind = m[1] ? 'comment' : m[2] ? 'string' : m[3] ? 'keyword' : m[4] ? 'type' : 'number';
    out.push(
      <span key={k++} style={{ color: colors[kind], fontStyle: kind === 'comment' ? 'italic' : undefined }}>
        {m[0]}
      </span>,
    );
    last = m.index + m[0].length;
  }
  out.push(code.slice(last));
  return out;
}

export default function Code({ code, file }) {
  return (
    <Box
      sx={{
        borderRadius: 2,
        border: '1px solid rgba(255,255,255,0.07)',
        background: 'rgba(0,0,0,0.45)',
        overflow: 'hidden',
      }}
    >
      {file && (
        <Box sx={{ px: 2, py: 0.75, fontFamily: mono, fontSize: '0.7rem', color: 'text.secondary', borderBottom: '1px solid rgba(255,255,255,0.06)' }}>
          {file}
        </Box>
      )}
      <Box
        component="pre"
        sx={{ m: 0, p: 2, overflowX: 'auto', fontFamily: mono, fontSize: { xs: '0.72rem', sm: '0.78rem' }, lineHeight: 1.65, color: '#D6D6D6' }}
      >
        <code>{highlight(code)}</code>
      </Box>
    </Box>
  );
}
