package fr.labomg.biophonie.feature.exploregeopoints

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import fr.labomg.biophonie.core.ui.compose.RectangleButton
import fr.labomg.biophonie.core.ui.compose.SurfacePreview
import fr.labomg.biophonie.core.ui.theme.spacing

@Composable
fun AboutButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RectangleShape,
        modifier = modifier.size(50.dp).border(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Image(
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            painter = painterResource(id = R.drawable.ic_pine),
            contentDescription = stringResource(R.string.about),
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.small)
        )
    }
}

@Composable
fun AboutDialog(
    shouldShowDialog: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) {}
    if (shouldShowDialog) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                modifier = modifier.padding(MaterialTheme.spacing.medium)
            ) {
                Column {
                    DialogHeader(onDismissRequest)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    DialogContent(launcher)
                }
            }
        }
    }
}

@Composable
private fun DialogContent(
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        Text(
            text = AnnotatedString.fromHtml(stringResource(R.string.biophonie_introduction)),
            textAlign = TextAlign.Justify,
            style = MaterialTheme.typography.bodyLarge,
            modifier =
                Modifier.align(Alignment.Start)
                    .padding(
                        start = MaterialTheme.spacing.small,
                        end = MaterialTheme.spacing.small,
                        top = MaterialTheme.spacing.medium
                    )
        )
        HorizontalDivider(
            thickness = 1.dp,
            modifier =
                Modifier.padding(
                    horizontal = MaterialTheme.spacing.large,
                    vertical = MaterialTheme.spacing.medium
                )
        )
        Image(painter = painterResource(R.drawable.ic_pine), contentDescription = null)
        Text(stringResource(R.string.licence))
        Text(stringResource(R.string.gnu_gpl_v3_0))
        Text(stringResource(R.string.developped_location))
        RectangleButton(
            onClick = openBrowser(launcher, "https://labo.mg/"),
            modifier = Modifier.padding(vertical = MaterialTheme.spacing.small)
        ) {
            Text(stringResource(R.string.go_to_labo_mg))
        }
    }
}

private fun openBrowser(
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    url: String
): () -> Unit = {
    val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) }
    launcher.launch(intent)
}

@Composable
private fun DialogHeader(onDismissRequest: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min)
    ) {
        Text(
            text = stringResource(R.string.about_biophonie),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = MaterialTheme.spacing.small)
        )
        Button(
            shape = RectangleShape,
            colors = ButtonDefaults.outlinedButtonColors(),
            contentPadding = PaddingValues(0.dp),
            onClick = onDismissRequest,
            modifier = Modifier.fillMaxHeight()
        ) {
            Image(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.close)
            )
        }
    }
}

@Preview
@Composable
private fun AboutDialogPreview() {
    SurfacePreview { AboutDialog(shouldShowDialog = true, onDismissRequest = {}) }
}

@Preview
@Composable
private fun AboutButtonPreview() {
    SurfacePreview {
        AboutButton(modifier = Modifier.padding(MaterialTheme.spacing.medium), onClick = {})
    }
}
