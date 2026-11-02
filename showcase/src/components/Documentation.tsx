import { Box, Button, Paper, Stack, Typography } from '@mui/material';
import DescriptionOutlinedIcon from '@mui/icons-material/DescriptionOutlined';
import FileDownloadOutlinedIcon from '@mui/icons-material/FileDownloadOutlined';
import { alpha } from '@mui/material/styles';
import { DOC_URL, documentation } from '../data';
import { ember, mono } from '../theme';

export default function Documentation() {
  return (
    <Paper sx={{ p: { xs: 2.5, sm: 3 }, borderColor: alpha(ember, 0.3) }}>
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1.2fr' }, gap: 3 }}>
        <Box>
          <Typography sx={{ fontFamily: mono, fontSize: '0.7rem', color: 'text.secondary', mb: 1 }}>{documentation.meta}</Typography>
          <Typography variant="h3" sx={{ fontSize: '1.2rem', mb: 0.5 }}>{documentation.title}</Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2.5 }}>{documentation.course}</Typography>
          <Stack direction="row" sx={{ flexWrap: 'wrap', gap: 1 }}>
            <Button href={DOC_URL} target="_blank" rel="noopener" variant="contained" startIcon={<DescriptionOutlinedIcon />}>
              Open PDF
            </Button>
            <Button href={DOC_URL} download variant="outlined" startIcon={<FileDownloadOutlinedIcon />}>
              Download
            </Button>
          </Stack>
        </Box>
        <Box component="ol" sx={{ m: 0, p: 0, listStyle: 'none' }}>
          {documentation.chapters.map(([n, text]) => (
            <Box component="li" key={n} sx={{ display: 'flex', gap: 2, py: 1, borderTop: '1px solid rgba(255,255,255,0.05)', '&:first-of-type': { borderTop: 'none' } }}>
              <Typography sx={{ fontFamily: mono, fontSize: '0.78rem', color: 'primary.main', minWidth: 28 }}>{n}</Typography>
              <Typography variant="body2">{text}</Typography>
            </Box>
          ))}
        </Box>
      </Box>
    </Paper>
  );
}
