import {NodeWizardMode} from '../../enums/node_wizard_mode.enum';
import {NodeAddType} from '../../enums/node_wizard_add_type.enum';
import {NodeItem} from '../node_item.model';

export class NodeWizardForm {
  currentMode: NodeWizardMode;
  addType: NodeAddType;
  multipleNodeAdd: boolean;
  multipleNodes: NodeItem[];
  singleNode: NodeItem;
  finished: boolean;
  summaryComplete: boolean;


  constructor() {
    this.currentMode = NodeWizardMode.Start;
    this.finished = false;
    this.summaryComplete = false;
    this.multipleNodeAdd = false;

  }
}
