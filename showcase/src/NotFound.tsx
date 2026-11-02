import { Box, Button, Paper, Typography } from '@mui/material';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import { mono } from './theme';
import Backdrop from './components/Backdrop';

// Served by GitHub Pages for any path that doesn't exist.
export default function NotFound() {
  const path = window.location.pathname;

  return (
    <>
      <Backdrop />
      <Box component="main" sx={{ minHeight: '100vh', display: 'grid', placeItems: 'center', px: 2 }}>
        <Paper sx={{ p: { xs: 3, sm: 4 }, width: '100%', maxWidth: 460, animation: 'fadeIn .25s ease' }}>
          <Typography sx={{ fontFamily: mono, fontSize: '0.75rem', color: 'text.secondary', mb: 1, wordBreak: 'break-all' }}>
            <Box component="span" sx={{ color: 'primary.main' }}>$</Box> cd {path}
          </Typography>
          <Typography variant="h1" component="h1" sx={{ mb: 1 }}>
            404
          </Typography>
          <Typography color="text.secondary" sx={{ mb: 3 }}>
            This page doesn’t exist.
          </Typography>
          <Button href="/" variant="contained" startIcon={<ArrowBackRoundedIcon />}>
            Back to home
          </Button>
        </Paper>
      </Box>
    </>
  );
}
