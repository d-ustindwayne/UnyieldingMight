import { Box, Paper, Typography } from '@mui/material';
import { alpha } from '@mui/material/styles';
import { decision } from '../data';
import { ember, mono } from '../theme';

// Architecture decision record: the options the coursework weighed and where each version landed.
export default function Decision() {
  return (
    <Paper sx={{ p: { xs: 2, sm: 2.5 }, mb: 3 }}>
      <Typography sx={{ fontFamily: mono, fontSize: '0.7rem', color: 'text.secondary', mb: 1.5 }}>ADR-001 · architecture style</Typography>
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' }, gap: 1.25, mb: 2 }}>
        {decision.options.map((o, i) => {
          const chosen = i === 2;
          return (
            <Box
              key={o.name}
              sx={{
                borderRadius: 2, p: 1.5, border: '1px solid',
                borderColor: chosen ? alpha(ember, 0.5) : 'rgba(255,255,255,0.07)',
                background: chosen ? alpha(ember, 0.06) : 'rgba(0,0,0,0.25)',
              }}
            >
              <Typography sx={{ fontFamily: mono, fontSize: '0.82rem', mb: 0.75, color: chosen ? 'primary.main' : 'text.primary' }}>
                {o.name}{i === 1 ? ' · coursework' : chosen ? ' · rewrite' : ''}
              </Typography>
              <Typography variant="body2" sx={{ color: '#BFE8CD' }}>+ {o.pro}</Typography>
              <Typography variant="body2" sx={{ color: '#E6A9A9' }}>- {o.con}</Typography>
            </Box>
          );
        })}
      </Box>
      <Typography variant="body2" color="text.secondary">{decision.then} {decision.now}</Typography>
    </Paper>
  );
}
